from app.tools.stock_tools import (
    get_stock_prices,
    get_company_info,
    get_stock_statistics
)

from app.tools.rag_tools import (
    search_stock_knowledge
)


TOOLS = {

    "get_stock_prices":
        get_stock_prices,

    "get_company_info":
        get_company_info,

    "get_stock_statistics":
        get_stock_statistics,

    "search_stock_knowledge":
        search_stock_knowledge
}


def execute_tool(
        tool_name: str,
        arguments: dict
):

    tool = TOOLS.get(tool_name)

    if not tool:
        raise ValueError(
            f"Unknown tool: {tool_name}"
        )

    return tool.invoke(arguments)