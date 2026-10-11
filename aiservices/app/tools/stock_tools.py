import os
from datetime import date
from typing import Any

import requests
from langchain_core.tools import tool

SPRING_BOOT_URL = os.getenv("SPRING_BOOT_URL", "http://localhost:8080").rstrip("/")
HTTP_TIMEOUT_SECONDS = float(os.getenv("SPRING_BOOT_TIMEOUT_SECONDS", 20))


def _normalize_symbol(symbol: str) -> str:
    if symbol is None or not isinstance(symbol, str):
        raise TypeError("Stock symbol is required and must be a string.")

    normalized = symbol.strip()
    if not normalized:
        raise ValueError("Stock symbol must be a non-empty string.")

    if not normalized.replace("-", "").replace(".", "").isalnum():
        raise ValueError("Stock symbol contains invalid characters.")

    return normalized.upper()


def _parse_iso_date(value: Any, field_name: str) -> date:
    if value is None or not isinstance(value, str):
        raise TypeError(f"{field_name} must be provided as an ISO date string.")

    try:
        return date.fromisoformat(value)
    except ValueError as exc:
        raise ValueError(f"{field_name} must be a valid ISO date in YYYY-MM-DD format.") from exc


def _validate_date_range(from_date: Any, to_date: Any) -> tuple[date, date]:
    start_date = _parse_iso_date(from_date, "from_date")
    end_date = _parse_iso_date(to_date, "to_date")

    if start_date > end_date:
        raise ValueError("from_date must be on or before to_date.")

    return start_date, end_date


def _tool_error(error_type: str, message: str, **details: Any) -> dict[str, Any]:
    payload: dict[str, Any] = {
        "ok": False,
        "error": {
            "type": error_type,
            "message": message,
        },
    }
    if details:
        payload["error"].update(details)
    return payload


def _request_json(method: str, url: str, params: dict[str, Any] | None = None) -> dict[str, Any] | list[Any] | Any:
    try:
        response = requests.request(method, url, params=params, timeout=HTTP_TIMEOUT_SECONDS)
    except requests.Timeout:
        return _tool_error("timeout", f"Request to {url} timed out after {HTTP_TIMEOUT_SECONDS} seconds.")
    except requests.ConnectionError:
        return _tool_error("connection_error", f"Could not connect to {url}.")
    except requests.RequestException as exc:
        return _tool_error("request_error", f"Request to {url} failed: {exc}")

    try:
        response.raise_for_status()
    except requests.HTTPError as exc:
        status_code = exc.response.status_code if exc.response is not None else None
        response_text = exc.response.text if exc.response is not None else ""
        return _tool_error(
            "http_error",
            f"Spring Boot request failed with status {status_code}.",
            status_code=status_code,
            detail=response_text[:500],
        )

    try:
        payload = response.json()
    except ValueError:
        return _tool_error("invalid_json", f"Spring Boot response from {url} was not valid JSON.")

    if payload is None:
        return _tool_error("empty_response", f"Spring Boot response from {url} was empty.")

    return payload


@tool
def get_current_quote(symbol: str) -> dict:
    """Get the latest available market quote for a company."""
    normalized_symbol = _normalize_symbol(symbol)
    url = f"{SPRING_BOOT_URL}/api/stocks/{normalized_symbol}/quote"
    response = _request_json("GET", url)
    if isinstance(response, dict) and response.get("ok") is False:
        return response
    return response


@tool
def get_stock_prices(
        symbol: str,
        from_date: str,
        to_date: str
) -> dict:
    """
    Get historical stock prices for a company
    from the Spring Boot stock service.
    """
    normalized_symbol = _normalize_symbol(symbol)
    _validate_date_range(from_date, to_date)

    url = f"{SPRING_BOOT_URL}/api/stocks/{normalized_symbol}/prices"
    response = _request_json(
        "GET",
        url,
        params={
            "from": from_date,
            "to": to_date,
        },
    )

    if isinstance(response, dict) and response.get("ok") is False:
        return response

    if isinstance(response, list) and not response:
        return _tool_error("empty_data", "No historical stock prices were returned for the requested date range.")

    return response


@tool
def get_company_info(
        symbol: str
) -> dict:
    """
    Get company information using the stock symbol.

    Returns company details such as name, symbol,
    exchange, sector and industry.
    """
    normalized_symbol = _normalize_symbol(symbol)
    url = f"{SPRING_BOOT_URL}/api/stocks/companies/{normalized_symbol}"
    response = _request_json("GET", url)
    if isinstance(response, dict) and response.get("ok") is False:
        return response
    return response


@tool
def get_stock_statistics(
        symbol: str,
        from_date: str,
        to_date: str
) -> dict:
    """
    Get calculated historical stock statistics for a company.

    Returns start price, end price, highest price,
    lowest price, average price, price change,
    and price change percentage.
    """
    normalized_symbol = _normalize_symbol(symbol)
    _validate_date_range(from_date, to_date)

    url = f"{SPRING_BOOT_URL}/api/stocks/{normalized_symbol}/statistics"
    response = _request_json(
        "GET",
        url,
        params={
            "from": from_date,
            "to": to_date,
        },
    )

    if isinstance(response, dict) and response.get("ok") is False:
        return response

    if response in (None, {}, []) :
        return _tool_error("empty_data", "No stock statistics were returned for the requested date range.")

    return response


@tool
def get_technical_analysis(
        symbol: str,
        from_date: str,
        to_date: str,
        period: int = 14
) -> dict:
    """
    Get deterministic technical analysis indicators for a company.
    return structured SMA, EMA, RSI, ATR, volume Trends, Bollinger Bands,
    MACD when enough price history exists, and 52 weeks high/low values.
    These are calculated by Spring Boot code, not by the LLM.
    """
    normalized_symbol = _normalize_symbol(symbol)
    _validate_date_range(from_date, to_date)

    try:
        parsed_period = int(period)
    except (TypeError, ValueError) as exc:
        raise ValueError("period must be an integer greater than or equal to 2.") from exc

    if parsed_period < 2:
        raise ValueError("period must be at least 2.")

    url = f"{SPRING_BOOT_URL}/api/v1/stocks/{normalized_symbol}/technical"
    response = _request_json(
        "POST",
        url,
        params={
            "from": from_date,
            "to": to_date,
            "period": parsed_period,
        },
    )

    if isinstance(response, dict) and response.get("ok") is False:
        return response

    if response in (None, {}, []):
        return _tool_error("empty_data", "No technical analysis results were returned for the requested date range.")

    return response