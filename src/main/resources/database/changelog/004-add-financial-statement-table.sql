
CREATE TABLE financial_statement (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

     company_id UUID NOT NULL,

     fiscal_year INTEGER NOT NULL,
     fiscal_quarter INTEGER NOT NULL,
     report_date DATE NOT NULL,

     revenue NUMERIC(18,2),
     operating_income NUMERIC(18,2),
     ebit NUMERIC(18,2),
     interest_expense NUMERIC(18,2),
     taxes NUMERIC(18,2),
     net_income NUMERIC(18,2),

     operating_cash_flow NUMERIC(18,2),
     capital_expenditures NUMERIC(18,2),
     free_cash_flow NUMERIC(18,2),

     total_assets NUMERIC(18,2),
     total_liabilities NUMERIC(18,2),
     total_equity NUMERIC(18,2),
     total_debt NUMERIC(18,2),
     cash NUMERIC(18,2),
     working_capital NUMERIC(18,2),

     source VARCHAR(50),

     created_at TIMESTAMP DEFAULT NOW(),
     updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (company_id, fiscal_year, fiscal_quarter)

     CONSTRAINT fk_financial_statement_company
         FOREIGN KEY (company_id)
             REFERENCES company(id)
);
