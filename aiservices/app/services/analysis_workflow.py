


from datetime import date
from typing import Any, TypedDict

from langgraph.graph import END, StateGraph
from langchain_core.messages import (
    HumanMessage,SystemMessage
    )

from app.llm.ollama import llm
from app.models.tool_decision import ToolDecision
from app.services.tool_router import execute_tool


structured_llm = llm.with_structured_output(ToolDecision)


class AnalysisState(TypedDict, total=False):
    question: str
    decision: ToolDecision
    tool_results: list[dict[str, Any]]
    response: str | None
    tool_used: list[str]
    grounded: bool
    grounding_issue: list[str]
    sources: list[str]

def run_analysis_workflow(question: str) -> dict[str, Any]:
    initial_state: AnalysisState = {
        "question": question,
        "tool_results": [],
        "tool_used": [],
        "grounded": True,
        "grounding_issue": [],
        "sources": []
    }

    # =====================================
    # STEP 1 — TOOL DECISION
    # =====================================
    if StateGraph is None:
        return _run_sequential_workflow(initial_state)

    workflow = StateGraph(AnalysisState)
    workflow.add_node("plan", _plan_tools)
    workflow.add_node("direct_response", _direct_response)
    workflow.add_node("execute_tools", _execute_tools)
    workflow.add_node("generate_answer", _generate_answer)
    workflow.add_node("validate_grounding", _validate_grounding)

    workflow.set_entry_point("plan")
    workflow.add_conditional_edges("plan", 
                                   _route_after_planning,
                                   {
                                       "direct_response": "direct_response",
                                       "execute_tools": "execute_tools"
                                   })
    workflow.add_edge("direct_response", "generate_answer")
    workflow.add_edge("execute_tools", "generate_answer")
    workflow.add_edge("generate_answer", "validate_grounding")
    workflow.add_edge("validate_grounding", "generate_answer", END)

    compile_workflow = workflow.compile()
    final_state = compile_workflow(initial_state)
    return _to_response(final_state)

def _run_sequential_workflow(state: AnalysisState) -> dict[str, Any]:
    state = _plan_tools(state)
    if _route_after_planning(state) == "direct_response":
        state = _direct_response(state)
    else:
        state = _execute_tools(state)
        state = _generate_answer(state)
    state = _generate_answer(state)
    state = _validate_grounding(state)
    return _to_response(state)

def _plan_tools(state: AnalysisState) -> AnalysisState:
    decision_message = [
        SystemMessage(content=_planning_prompt),
        HumanMessage(content=state["question"])
    ]

    decision = structured_llm.invoke(decision_message)

    print("\n========== PLANNER ==========")
    print(decision.model_dump_json(indent=2))

    return {
        **state,
        "decision": decision,
        "tool_used": [tool_call.tool_name for tool_call in decision.tool_calls]
    }

def _route_after_planning(state: AnalysisState) -> str:
    decision = state["decision"]
    return "execute_tools" if decision.tool_calls else "direct_response"

def _direct_response(state: AnalysisState) -> AnalysisState:
    decision = state["decision"]
    response = decision.response or "I can help with stock analysis, price, fundamentals, technical Indicators, and knowledge based questions."
    return {
        **state,
        "response": response,
        "grounded": True
    }

def _execute_tools(state: AnalysisState) -> AnalysisState:
    print("\n========== EXECUTING TOOLS ==========")
    tool_results = []

    for tool_call in state["decision"].tool_calls:
        print(f"\nTool: {tool_call.tool_name}")
        print(f"Arguments: {tool_call.arguments}")

        try:
            result = execute_tool(tool_call.tool_name, tool_call.arguments)
            tool_results.append({
                "tool_name": tool_call.tool_name,
                "arguments": tool_call.arguments,
                "result": result
            })

        except Exception as e:
            print(f"Error executing tool {Exception}")
            tool_results.append({
                "tool_name": tool_call.tool_name,
                "arguments": tool_call.arguments,
                "result": f"Error: {e}"
            })

    print("\n===============TOOL RESULTS===============\n")
    for result in tool_results:
        print(f"Result: {result['result']}\n")

    return {
        **state,
        "tool_results": tool_results,
        "sources": _extract_sources(tool_results)
    }

def _generate_answer(state: AnalysisState) -> AnalysisState:
   if not _has_successful_tool_results(state["tool_results"]):
        return {
            **state,
            "response": "I could not retrieve enough grounded information to answer your question. Please try again later or ask a different question.",
            "grounded": False,
            "grounding_issue": ["No successful tool results was available to generate a grounded response."]
        }

   final_message = [
        SystemMessage(content=_answer_prompt),
        HumanMessage(content = f"""
User Question: {state["question"]}
Tool Results: {state["tool_results"]}
Now please provide a comprehensive and grounded answer to the user's question based on the tool results. 
If the tool results are insufficient to provide a grounded answer, please indicate that in your response.
""")
    ]

   final_response = llm.invoke(final_message)
   return {
       **state,
       "response": final_response.content,
   }


def _validate_grounding(state: AnalysisState) -> AnalysisState:

    tool_results = state.get("tool_results", [])
    response = state.get("response") or ""
    issues = list(state.get("grounding_issue", []))

    if tool_results and not _has_successful_tool_results(tool_results):
        issues.append("Tool results were not sufficient to provide a grounded response.")

    unavailable_results = [
        result for result in tool_results
            if isinstance(result.get("result"), dict) and result["result"].get("found", "") is False
    ]

    if unavailable_results:
        issues.append(f"{len(unavailable_results)} one or more tools returned found=false, indicating that the requested information is not available in the current knowledge base.")

    sources = state.get("sources", [])
    if sources and "sources" in response.lower():
        response = f"{response.rsplit()}\n\nSources: {', '.join(sources)}"

    return {
        **state,
        "response": response,
        "grounded": len(issues) == 0,
        "grounding_issue": issues
    }

def _has_successful_tool_results(tool_results: list[dict[str, Any]]) -> bool:
    for result in tool_results:
        if result.get("error"):
            continue
        payload = result.get("result")
        if isinstance(payload, dict) and payload.get("found") is False:
            continue
        if payload not in (None, [], {}):
            return True
    return False

def _extract_sources(tool_results: list[dict[str, Any]]) -> list[str]:
    sources = []

    for tool_result in tool_results:
        payload = tool_result.get("result")
        if isinstance(payload, dict) and payload.get("found") is False:
            continue

        for item in payload.get("results", []):
            source = item.get("source")
            if source and source not in sources:
                sources.append(source)

    return sources

def _to_response(state: AnalysisState) -> dict[str, Any]:
    return {
        "response": state.get("response"),
        "tools_used": state.get("tools_used", []),
        "tools_results": state.get("tools_results", []),
        "grounded": state.get("grounded", True),
        "grounding_issues": state.get("grounding_issues", []),
        "sources": state.get("sources", [])
    }


def _planning_prompt() -> str:
    today = date.today().isoformat()
    return f"""
You are a Stock AI assistant.

Today's Date is {today}





"""

def _answer_prompt(sources: list[str]) -> str:
    source_rule = ""
    if sources: 
        source_rule = f"\n- End with this Exact source list: Sources: {', '.join(sources)}"

        return f"""

"""