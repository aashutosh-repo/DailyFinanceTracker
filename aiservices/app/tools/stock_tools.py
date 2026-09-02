import requests

from langchain_core.tools import tool
import os

from psycopg.conninfo import timeout_from_conninfo

SPRING_BOOT_URL = os.getenv("SPRING_BOOT_URL","http://localhost:8080")
HTTP_TIMEOUT_SECONDS = float(os.getenv("SPRING_BOOT_TIMEOUT_SECONDS", 20))


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

    url = (
        f"{SPRING_BOOT_URL}"
        f"/api/stocks/{symbol}/prices"
    )

    response = requests.get(
        url,
        params={
            "from": from_date,
            "to": to_date
        },
        timeout=HTTP_TIMEOUT_SECONDS
    )

    response.raise_for_status()

    return response.json()

@tool
def get_company_info(
        symbol: str
) -> dict:
    """
    Get company information using the stock symbol.

    Returns company details such as name, symbol,
    exchange, sector and industry.
    """

    url = (
        f"{SPRING_BOOT_URL}"
        f"/api/stocks/companies/{symbol}"
    )

    response = requests.get(
        url,
        timeout=HTTP_TIMEOUT_SECONDS
    )

    response.raise_for_status()

    return response.json()

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

    url = (
        f"{SPRING_BOOT_URL}"
        f"/api/stocks/{symbol}/statistics"
    )

    response = requests.get(
        url,
        params={
            "from": from_date,
            "to": to_date
        },
        timeout=HTTP_TIMEOUT_SECONDS
    )

    response.raise_for_status()

    return response.json()

@tool
def get_technical_analysis(
        symbol: str,
        from_date: str,
        to_date: str,
        period: int=14
) -> dict:
    """
    Get deterministic technical analysis indicators for a company.
    return structured SMA, EMA, RSI, ATR, volume Trends, Bollinger Bands,
    MACD when enough price history exists, and 52 weeks high/low values.
    These are calculated by Spring Boot code, not by the LLM.
    """
    url = (
        f"(SPRING_BOOT_URL)"
        f"(/api/v1/stocks/{symbol}/technical)"
    )
    response = requests.get(
        url,
        params={
            "form": from_date,
            "to": to_date,
            "period": period
        },
        timeout=HTTP_TIMEOUT_SECONDS
    )
    response.raise_for_status()
    return response.json()