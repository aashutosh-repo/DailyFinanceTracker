from app.tools.stock_tools import (
    get_stock_prices,
    get_company_info,
    get_stock_statistics,
    get_technical_analysis,
    get_current_quote
)

from app.tools.rag_tools import (
    search_stock_knowledge
)
from app.services.tool_result import validate_tool_result


TOOLS = {

    "get_stock_prices":
        get_stock_prices,

    "get_company_info":
        get_company_info,

    "get_stock_statistics":
        get_stock_statistics,

    "get_technical_analysis":
        get_technical_analysis,

    "get_current_quote":
        get_current_quote,

    "search_stock_knowledge":
        search_stock_knowledge
}


def execute_tool(
        tool_name: str,
        arguments: dict
):

    if tool_name not in TOOLS:
        raise ValueError(f"Unknown tool: {tool_name}")

    if arguments is None:
        raise TypeError(f"Tool '{tool_name}' requires a dictionary of arguments.")

    if not isinstance(arguments, dict):
        raise TypeError(f"Tool '{tool_name}' expected a dictionary of arguments.")

    try:
        result = TOOLS[tool_name].invoke(arguments)
    except (TypeError, ValueError) as exc:
        raise ValueError(f"Invalid arguments for tool '{tool_name}': {exc}") from exc
    except Exception as exc:  # pragma: no cover - defensive for runtime tool faults
        raise RuntimeError(f"Tool '{tool_name}' failed: {type(exc).__name__}: {exc}") from exc

    return validate_tool_result(tool_name, arguments, result)