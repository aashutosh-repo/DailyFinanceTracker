from datetime import date

from langchain_core.messages import (
    HumanMessage,
    SystemMessage
)

from app.llm.ollama import llm
from app.models.tool_decision import ToolDecision
from app.services.tool_router import execute_tool


# =====================================
# Structured LLM
# =====================================

structured_llm = llm.with_structured_output(
    ToolDecision
)


def ask_assistant(question: str):

    # =====================================
    # STEP 1 — Decide what to do
    # =====================================

    today = date.today().isoformat()

    planning_prompt = f"""
You are a Stock AI Assistant.

Today's date is {today}.

Your responsibility is ONLY to analyze the user's request
and decide which tools are required.

IMPORTANT:
You are a PLANNER, not the final answer generator.

DO NOT answer the user's question from your own knowledge.

You may select ZERO, ONE, or MULTIPLE tools.

Available tools:

1. get_stock_prices

Use for:
- historical prices
- daily prices
- price records

Arguments:
- symbol
- from_date
- to_date


2. get_company_info

Use for:
- company details stored in the application
- sector
- industry
- exchange

Arguments:
- symbol


3. get_stock_statistics

Use for:
- stock performance
- price performance
- highest price
- lowest price
- average price
- price change
- percentage change
- historical performance

Arguments:
- symbol
- from_date
- to_date


4. search_stock_knowledge

Use for:
- what a company does
- company business
- company services
- company overview
- financial concepts
- stock analysis concepts
- information from the knowledge base

Arguments:
- question
- company (optional)


MANDATORY RULES:

- NEVER answer company business or company service questions directly.
- ALWAYS use search_stock_knowledge for:
  - What does a company do?
  - What services does a company provide?
  - Company overview
  - Company business information
- NEVER use your own knowledge for these questions.
- You are ONLY responsible for selecting tools.
- Use the minimum number of tools required.
- You may select multiple tools.
- Do not select duplicate tools.
- If the user asks multiple independent questions,
  create a separate tool call for each requirement.
- If a date is provided without a year,
  resolve it using today's date.
- Use ISO date format: YYYY-MM-DD.
- Only return an empty tool_calls list if the user is asking
  a conversational question that requires no external information.

EXAMPLE:

User:
"What services does TCS provide?"

Correct decision:

tool_calls:
[
    {{
        "tool_name": "search_stock_knowledge",
        "arguments": {{
            "question": "What services does TCS provide?",
            "company": "TCS"
        }}
    }}
]

response: null
"""

    decision_messages = [

        SystemMessage(
            content=planning_prompt
        ),

        HumanMessage(
            content=question
        )
    ]


    # =====================================
    # STEP 2 — Get Tool Decision
    # =====================================

    decision = structured_llm.invoke(
        decision_messages
    )


    print("\n===== TOOL DECISION =====")

    print(
        decision.model_dump_json(
            indent=2
        )
    )


    # =====================================
    # STEP 3 — Direct Response
    # =====================================

    if not decision.tool_calls:

        return {
            "response": decision.response,
            "tools_used": [],
            "tool_results": []
        }


    # =====================================
    # STEP 4 — Execute Tools
    # =====================================

    print("\n===== EXECUTING TOOLS =====")

    tool_results = []


    for tool_call in decision.tool_calls:

        print(
            f"\nExecuting: {tool_call.tool_name}"
        )

        print(
            f"Arguments: {tool_call.arguments}"
        )


        try:

            result = execute_tool(
                tool_call.tool_name,
                tool_call.arguments
            )


            tool_results.append({

                "tool_name":
                    tool_call.tool_name,

                "arguments":
                    tool_call.arguments,

                "result":
                    result

            })


        except Exception as exception:

            print(
                f"Tool failed: {exception}"
            )


            tool_results.append({

                "tool_name":
                    tool_call.tool_name,

                "arguments":
                    tool_call.arguments,

                "error":
                    str(exception)

            })


    print("\n===== TOOL RESULTS =====")

    for result in tool_results:

        print(result)


    # =====================================
    # STEP 5 — Ask LLM to analyze results
    # =====================================

    final_system_prompt = """
You are a helpful Stock AI Assistant.

Answer the user's question using the provided tool results.

Rules:

- Use ONLY the provided tool results for factual claims.
- Do not invent stock prices, statistics,
  company information, or financial facts.
- If multiple tools were used, combine the
  information naturally.
- If a RAG tool returns found=false,
  clearly tell the user that the requested
  information is not available in the
  current knowledge base.
- Do not fill missing information using
  your own knowledge.
- If a tool returned an error,
  explain that the information could not
  be retrieved.
- Keep the answer clear and concise.
"""


    final_messages = [

        SystemMessage(
            content=final_system_prompt
        ),

        HumanMessage(
            content=f"""
User Question:

{question}

Tool Results:

{tool_results}

Now provide the final answer.
"""
        )
    ]


    # =====================================
    # STEP 6 — Generate Final Answer
    # =====================================

    final_response = llm.invoke(
        final_messages
    )


    # =====================================
    # STEP 7 — Return Response
    # =====================================

    return {

        "response":
            final_response.content,

        "tools_used": [
            tool_call.tool_name
            for tool_call in decision.tool_calls
        ],

        "tool_results":
            tool_results
    }