import requests

from langchain_core.tools import tool


SPRING_BOOT_URL = "http://localhost:8080"


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
        timeout=10
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
        timeout=10
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
        timeout=10
    )

    response.raise_for_status()

    return response.json()