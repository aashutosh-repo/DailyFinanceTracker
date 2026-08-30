from datetime import date

from langchain_core.messages import (
    HumanMessage,
    SystemMessage
)

from app.llm.ollama import llm

from app.models.tool_decision import (
    ToolDecision
)

from app.services.tool_router import (
    execute_tool
)


structured_llm = llm.with_structured_output(
    ToolDecision
)


def ask_assistant(question: str):

    # =====================================
    # STEP 1 — PLAN
    # =====================================

    today = date.today().isoformat()

    planning_prompt = f"""
You are a Stock AI Assistant.

Today's date is {today}.

Your responsibility is to understand the
user's request and decide which tools are
required.

You may select:
- ZERO tools
- ONE tool
- MULTIPLE tools

Use the minimum number of tools required.

Available tools:

--------------------------------------------------
1. get_stock_prices
--------------------------------------------------

Use for:
- historical prices
- daily prices
- opening price
- closing price
- high price
- low price
- volume

Arguments:

symbol
from_date
to_date


--------------------------------------------------
2. get_company_info
--------------------------------------------------

Use for:
- company information
- company sector
- industry
- exchange

Arguments:

symbol


--------------------------------------------------
3. get_stock_statistics
--------------------------------------------------

Use for:
- stock performance
- stock price performance
- price change
- percentage change
- highest price
- lowest price
- average price

Arguments:

symbol
from_date
to_date


--------------------------------------------------
4. search_stock_knowledge
--------------------------------------------------

Use for:
- company business
- company services
- company overview
- company background
- financial concepts
- stock analysis concepts
- information from the knowledge base


--------------------------------------------------
5. get_technical_analysis
--------------------------------------------------

Use for:
- technical Indicator
- RSI
- MACD
- SMA or EMA
- Boillinger Bands
- ATR
- Volume trend
- 52-week High or Low
- moving average signals
Arguments:

question
company


--------------------------------------------------
MULTI TOOL RULES
--------------------------------------------------

If the user asks for information that requires
multiple independent data sources, use multiple tools.

Example:

User:
"How did TCS perform last week and what
services does TCS provide?"

Tools:

1. get_stock_statistics

2. search_stock_knowledge


Example:

User:
"Show TCS prices and explain what the
company does."

Tools:

1. get_stock_prices

2. search_stock_knowledge


--------------------------------------------------
DATE RULES
--------------------------------------------------

If the user provides dates without a year,
resolve them using today's date.

Do not invent dates.

If dates are required but missing,
do not call the tool.

Instead return a response asking the user
for the required date range.


--------------------------------------------------
GENERAL RULES
--------------------------------------------------

- Do not select duplicate tools.
- Use the minimum number of tools.
- Each tool should solve one requirement.
- If no tool is needed,
  return an empty tool_calls list.
- Provide a direct response only when
  no tool is required.
"""

    decision_messages = [

        SystemMessage(
            content=planning_prompt
        ),

        HumanMessage(
            content=question
        )
    ]


    decision = structured_llm.invoke(
        decision_messages
    )


    print("\n========== PLANNER ==========")

    print(
        f"Tool Calls: "
        f"{len(decision.tool_calls)}"
    )

    for tool_call in decision.tool_calls:

        print(
            f"\nTool: "
            f"{tool_call.tool_name}"
        )

        print(
            f"Arguments: "
            f"{tool_call.arguments}"
        )

    print("\n=============================\n")


    # =====================================
    # STEP 2 — DIRECT RESPONSE
    # =====================================

    if not decision.tool_calls:

        return {

            "response":
                decision.response,

            "tools_used":
                [],

            "tool_results":
                []
        }


    # =====================================
    # STEP 3 — EXECUTE TOOLS
    # =====================================

    tool_results = []


    for tool_call in decision.tool_calls:

        print(
            f"\n===== EXECUTING TOOL ====="
        )

        print(
            f"Tool Name: "
            f"{tool_call.tool_name}"
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
                f"Tool Error: {exception}"
            )


            tool_results.append({

                "tool_name":
                    tool_call.tool_name,

                "arguments":
                    tool_call.arguments,

                "error":
                    str(exception)
            })


    print("\n========== TOOL RESULTS ==========")

    for result in tool_results:

        print(result)

    print("\n==================================\n")


    # =====================================
    # STEP 4 — FINAL ANALYSIS
    # =====================================

    final_system_prompt = """
You are a helpful Stock AI Assistant.

Answer the user's question using ONLY the
provided tool results.

Rules:

- Use ONLY the tool results for factual claims.
- Do not invent stock prices.
- Do not invent statistics.
- Do not invent company facts.
- Do not use outside knowledge.

MULTIPLE TOOLS:

- If multiple tools were used,
  combine the information naturally.
- Clearly connect related information.
- Do not unnecessarily repeat the raw tool output.

RAG RESULTS:

- If a RAG tool returns found=false,
  clearly state that the requested information
  is not available in the current knowledge base.
- Do not invent missing information.

ERRORS:

- If a tool returned an error,
  explain that the information could not
  be retrieved.

STOCK ANALYSIS:

- Historical performance does not guarantee
  future performance.
- Do not present predictions as certainty.

Keep the answer clear and concise.
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


    final_response = llm.invoke(
        final_messages
    )


    # =====================================
    # STEP 5 — RETURN RESPONSE
    # =====================================

    return {

        "response":
            final_response.content,

        "tools_used":
            [
                tool_call.tool_name
                for tool_call in decision.tool_calls
            ],

        "tool_results":
            tool_results
    }