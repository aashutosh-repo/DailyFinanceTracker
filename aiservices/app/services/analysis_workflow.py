


import logging
import re
from datetime import date
from decimal import Decimal, InvalidOperation
from typing import Any, TypedDict

try:
    from langgraph.graph import END, StateGraph
except ModuleNotFoundError:  # The sequential execution path supports minimal environments.
    END = None
    StateGraph = None
from langchain_core.messages import (
    HumanMessage,SystemMessage
    )

from app.llm.ollama import llm
from app.models.tool_decision import ToolDecision
from app.services.tool_router import execute_tool
from app.services.tool_result import validate_tool_result
from app.services.request_context import request_id_context


structured_llm = llm.with_structured_output(ToolDecision)
logger = logging.getLogger(__name__)


class AnalysisState(TypedDict, total=False):
    question: str
    decision: ToolDecision
    tool_results: list[dict[str, Any]]
    response: str | None
    tool_used: list[str]
    grounded: bool
    grounding_issue: list[str]
    sources: list[str]
    status: str
    evidence_sufficient: bool
    data_quality_limitations: list[str]
    evidence_references: list[str]
    synthetic_data: bool
    provider_metadata: list[dict[str, Any]]
    numerical_claims_valid: bool

def run_analysis_workflow(question: str) -> dict[str, Any]:
    initial_state: AnalysisState = {
        "question": question,
        "tool_results": [],
        "tool_used": [],
        "grounded": True,
        "grounding_issue": [],
        "sources": []
    }

    try:
        if StateGraph is None:
            return _run_sequential_workflow(initial_state)

        workflow = StateGraph(AnalysisState)
        workflow.add_node("plan", _plan_tools)
        workflow.add_node("direct_response", _direct_response)
        workflow.add_node("execute_tools", _execute_tools)
        workflow.add_node("insufficient_evidence", _insufficient_evidence)
        workflow.add_node("generate_answer", _generate_answer)
        workflow.add_node("validate_grounding", _validate_grounding)

        workflow.set_entry_point("plan")
        workflow.add_conditional_edges(
            "plan",
            _route_after_planning,
            {"direct_response": "direct_response", "execute_tools": "execute_tools"},
        )
        workflow.add_edge("direct_response", "validate_grounding")
        workflow.add_conditional_edges(
            "execute_tools",
            _route_after_execution,
            {"generate_answer": "generate_answer", "insufficient_evidence": "insufficient_evidence"},
        )
        workflow.add_edge("insufficient_evidence", "validate_grounding")
        workflow.add_edge("generate_answer", "validate_grounding")
        workflow.add_edge("validate_grounding", END)

        final_state = workflow.compile().invoke(initial_state)
        return _to_response(final_state)
    except Exception as exc:
        logger.error(
            "Stock analysis workflow failed",
            extra={"request_id": request_id_context.get(), "failure_category": type(exc).__name__},
        )
        return _error_response("The stock analysis could not be completed because an internal service failed.")

def _run_sequential_workflow(state: AnalysisState) -> dict[str, Any]:
    state = _plan_tools(state)
    if _route_after_planning(state) == "direct_response":
        state = _direct_response(state)
    else:
        state = _execute_tools(state)
        if _route_after_execution(state) == "generate_answer":
            state = _generate_answer(state)
        else:
            state = _insufficient_evidence(state)
    state = _validate_grounding(state)
    return _to_response(state)

def _plan_tools(state: AnalysisState) -> AnalysisState:
    decision_message = [
        SystemMessage(content=_planning_prompt()),
        HumanMessage(content=state["question"])
    ]

    decision = structured_llm.invoke(decision_message)

    logger.info(
        "Stock analysis tools planned",
        extra={
            "request_id": request_id_context.get(),
            "selected_tools": [call.tool_name for call in decision.tool_calls],
        },
    )

    return {
        **state,
        "decision": decision,
        "tool_used": [tool_call.tool_name for tool_call in decision.tool_calls]
    }

def _route_after_planning(state: AnalysisState) -> str:
    decision = state["decision"]
    return "execute_tools" if decision.tool_calls else "direct_response"

def _direct_response(state: AnalysisState) -> AnalysisState:
    response = "I could not identify a data source needed to support this stock analysis. Please ask for a specific company, metric, or period."
    return {
        **state,
        "response": response,
        "grounded": False,
        "status": "ERROR",
        "evidence_sufficient": False,
        "grounding_issue": ["The planner selected no evidence-producing tools."],
        "data_quality_limitations": ["No retrieved evidence is available for factual stock claims."],
    }

def _execute_tools(state: AnalysisState) -> AnalysisState:
    tool_results = []

    for tool_call in state["decision"].tool_calls:
        try:
            result = execute_tool(tool_call.tool_name, tool_call.arguments)
            if not isinstance(result, dict) or result.get("status") not in {"SUCCESS", "PARTIAL", "EMPTY", "ERROR"}:
                result = validate_tool_result(tool_call.tool_name, tool_call.arguments, result)
        except Exception as exc:
            result = validate_tool_result(
                tool_call.tool_name,
                tool_call.arguments,
                {"ok": False, "error": {"type": type(exc).__name__, "message": "Tool execution failed."}},
            )
        tool_results.append(result)
        logger.info(
            "Stock analysis tool completed",
            extra={
                "tool_name": tool_call.tool_name,
                "request_id": request_id_context.get(),
                "tool_status": result["status"],
                "provider_status": result["provider_status"],
                "synthetic_data": result["synthetic_data"],
                "error_category": result.get("error_category"),
                "limitation_count": len(result.get("limitations", [])),
            },
        )

    evidence_sufficient = _has_successful_tool_results(tool_results)
    logger.info(
        "Stock analysis evidence evaluated",
        extra={"evidence_sufficient": evidence_sufficient, "tool_count": len(tool_results)},
    )

    return {
        **state,
        "tool_results": tool_results,
        "sources": _extract_sources(tool_results),
        "evidence_sufficient": evidence_sufficient,
        "status": _aggregate_status(tool_results),
        "data_quality_limitations": _collect_limitations(tool_results),
        "evidence_references": _collect_references(tool_results),
        "provider_metadata": _collect_providers(tool_results),
        "synthetic_data": any(result.get("synthetic_data") for result in tool_results),
    }


def _route_after_execution(state: AnalysisState) -> str:
    return "generate_answer" if _has_successful_tool_results(state.get("tool_results", [])) else "insufficient_evidence"


def _insufficient_evidence(state: AnalysisState) -> AnalysisState:
    results = state.get("tool_results", [])
    outcomes = [
        f"{item.get('tool_name', 'tool')}: {item.get('status', 'ERROR')}"
        + (f" ({item['error_category']})" if item.get("error_category") else "")
        for item in results
    ]
    reason = "; ".join(outcomes) or "no tools were selected"
    response = f"I could not complete the requested analysis because no usable evidence was available ({reason})."
    return {
        **state,
        "response": response,
        "status": "ERROR",
        "evidence_sufficient": False,
        "grounded": False,
        "grounding_issue": ["No successful or partial validated tool result was available."],
    }

def _generate_answer(state: AnalysisState) -> AnalysisState:
    usable_results = _usable_tool_results(state.get("tool_results", []))
    if not usable_results:
        return _insufficient_evidence(state)

    final_message = [
        SystemMessage(content=_answer_prompt(state.get("sources", []), state.get("data_quality_limitations", []))),
        HumanMessage(content=(
            f"User question: {state['question']}\n"
            f"Validated evidence only: {usable_results}\n"
            "Answer only claims supported by this evidence. Do not calculate or infer missing values. "
            "Clearly list requested metrics that are unavailable. Separate observations from interpretation."
        )),
    ]
    final_response = llm.invoke(final_message)
    response = final_response.content if isinstance(final_response.content, str) else str(final_response.content)
    unsupported_numbers = _unsupported_numbers(response, _evidence_numbers(usable_results))
    numerical_claims_valid = not unsupported_numbers
    if unsupported_numbers:
        response = "I retrieved relevant data, but the generated analysis included numerical claims I could not verify against that data, so I am withholding those claims. Review the returned evidence and limitations."
    limitations = state.get("data_quality_limitations", [])
    if limitations:
        response = f"{response}\n\nData limitations: " + " ".join(limitations)

    return {**state, "response": response, "numerical_claims_valid": numerical_claims_valid}


def _validate_grounding(state: AnalysisState) -> AnalysisState:
    tool_results = state.get("tool_results", [])
    response = state.get("response") or ""
    issues = list(state.get("grounding_issue", []))

    usable_results = _usable_tool_results(tool_results)
    if not usable_results:
        return _insufficient_evidence({**state, "response": response})

    if state.get("numerical_claims_valid") is False:
        issues.append("Generated numerical claims did not match validated tool evidence and were withheld.")

    sources = state.get("sources", [])
    if sources:
        response += f"\n\nSources: {', '.join(sources)}"

    status = state.get("status", _aggregate_status(tool_results))
    if issues and status == "SUCCESS":
        status = "PARTIAL"

    return {
        **state,
        "response": response,
        "grounded": not issues and bool(usable_results),
        "grounding_issue": list(dict.fromkeys(issues)),
        "status": status,
        "evidence_sufficient": bool(usable_results),
    }

def _has_successful_tool_results(tool_results: list[dict[str, Any]]) -> bool:
    return bool(_usable_tool_results(tool_results))


def _usable_tool_results(tool_results: list[dict[str, Any]]) -> list[dict[str, Any]]:
    return [
        result for result in tool_results
        if result.get("status") in {"SUCCESS", "PARTIAL"} and result.get("payload") not in (None, {}, [])
    ]


def _extract_sources(tool_results: list[dict[str, Any]]) -> list[str]:
    sources = []

    for result in _usable_tool_results(tool_results):
        payload = result.get("payload")
        if isinstance(payload, list):
            items = payload
        elif isinstance(payload, dict):
            items = payload.get("results", []) if isinstance(payload.get("results"), list) else [payload]
        else:
            items = []
        for item in items:
            if isinstance(item, dict):
                source = item.get("source")
                if isinstance(source, str) and source and source not in sources:
                    sources.append(source)
        provider = result.get("provider")
        if provider and provider not in sources:
            sources.append(provider)

    return sources

def _to_response(state: AnalysisState) -> dict[str, Any]:
    return {
        "response": state.get("response"),
        "tools_used": state.get("tool_used", []),
        "tools_results": state.get("tool_results", []),
        "grounded": state.get("grounded", True),
        "grounding_issues": state.get("grounding_issue", []),
        "sources": state.get("sources", []),
        "status": state.get("status", "ERROR"),
        "evidence_sufficient": state.get("evidence_sufficient", False),
        "tools_executed": state.get("tool_used", []),
        "provider_metadata": state.get("provider_metadata", []),
        "data_quality_limitations": state.get("data_quality_limitations", []),
        "evidence_references": state.get("evidence_references", []),
        "synthetic_data": state.get("synthetic_data", False),
    }


def _aggregate_status(tool_results: list[dict[str, Any]]) -> str:
    usable = _usable_tool_results(tool_results)
    if not usable:
        return "ERROR"
    if len(usable) != len(tool_results) or any(item.get("status") != "SUCCESS" for item in usable):
        return "PARTIAL"
    return "SUCCESS"


def _collect_limitations(tool_results: list[dict[str, Any]]) -> list[str]:
    limitations = []
    for item in tool_results:
        limitations.extend(item.get("limitations", []))
        if item.get("status") in {"ERROR", "EMPTY"}:
            limitations.append(f"{item.get('tool_name', 'Tool')} returned {item['status']} ({item.get('error_category') or 'no matching data'}).")
    return list(dict.fromkeys(limitations))


def _collect_references(tool_results: list[dict[str, Any]]) -> list[str]:
    return list(dict.fromkeys(
        reference
        for item in _usable_tool_results(tool_results)
        for reference in item.get("evidence_references", [])
    ))


def _collect_providers(tool_results: list[dict[str, Any]]) -> list[dict[str, Any]]:
    return [
        {
            "tool_name": item.get("tool_name"),
            "provider": item.get("provider"),
            "provider_status": item.get("provider_status", "UNKNOWN"),
            "data_classification": item.get("data_classification", "UNKNOWN"),
            "synthetic_data": item.get("synthetic_data", False),
        }
        for item in _usable_tool_results(tool_results)
    ]


def _evidence_numbers(results: list[dict[str, Any]]) -> set[Decimal]:
    numbers: set[Decimal] = set()

    def visit(value: Any) -> None:
        if isinstance(value, bool) or value is None:
            return
        if isinstance(value, (int, float, Decimal)):
            try:
                numbers.add(Decimal(str(value)).normalize())
            except InvalidOperation:
                pass
        elif isinstance(value, str):
            try:
                numbers.add(Decimal(value.replace(",", "")).normalize())
            except InvalidOperation:
                pass
        elif isinstance(value, dict):
            for nested in value.values():
                visit(nested)
        elif isinstance(value, list):
            for nested in value:
                visit(nested)

    for result in results:
        visit(result.get("payload"))
        visit(result.get("requested_parameters"))
        visit(result.get("requested_date_range"))
    return numbers


def _unsupported_numbers(response: str, allowed_numbers: set[Decimal]) -> list[str]:
    without_dates = re.sub(r"\b\d{4}-\d{2}-\d{2}\b", " ", response)
    unsupported = []
    for match in re.finditer(r"(?<![A-Za-z])[-+]?\d[\d,]*(?:\.\d+)?%?(?![A-Za-z])", without_dates):
        token = match.group().rstrip("%")
        try:
            number = Decimal(token.replace(",", "")).normalize()
        except InvalidOperation:
            continue
        if number not in allowed_numbers and token not in unsupported:
            unsupported.append(token)
    return unsupported


def _error_response(message: str) -> dict[str, Any]:
    return {
        "response": message,
        "tools_used": [],
        "tools_results": [],
        "grounded": False,
        "grounding_issues": ["The analysis workflow failed before validated evidence was available."],
        "sources": [],
        "status": "ERROR",
        "evidence_sufficient": False,
        "tools_executed": [],
        "provider_metadata": [],
        "data_quality_limitations": ["No validated evidence is available."],
        "evidence_references": [],
        "synthetic_data": False,
    }


def _planning_prompt() -> str:
    today = date.today().isoformat()
    return f"""
You are a Stock AI assistant.

Today's Date is {today}

Use get_current_quote for questions about the latest or current price, and use historical prices/statistics or technical analysis for past periods. Use company information and knowledge search for business or service questions. Do not calculate prices or indicators yourself; explain values returned by tools.





"""

def _answer_prompt(sources: list[str], limitations: list[str]) -> str:
    source_rule = ""
    if sources: 
        source_rule = f"\n- End with this Exact source list: Sources: {', '.join(sources)}"

    return f"""
You are a financial research assistant. Use only the validated evidence supplied in the user message for factual claims about the requested stock and period.
Never invent or recalculate prices, percentage changes, technical indicators, dates, volume, fundamentals, or provider information. Quote numerical values exactly as supplied.
Do not interpret missing data as zero or unchanged performance. Identify requested metrics that are unavailable.
Separate retrieved observations from interpretation. Historical results do not guarantee future performance and must not be presented as predictions.
If data is partial, restrict the analysis to the available fields and state the limitation. If provider provenance is unknown or synthetic, do not call it verified/live data.
Known data limitations: {limitations}
{source_rule}
"""