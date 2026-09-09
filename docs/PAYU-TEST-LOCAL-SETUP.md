# PayU TEST local setup

PayU visibility is controlled by `payment.providers.payu.enabled` in Spring configuration. It is enabled by default in TEST mode, so it appears in `GET /api/payments/providers` after the backend restarts. PayU credentials are still required before starting an actual transaction.

Set these environment variables in the terminal or run configuration used to start Spring Boot when you want to execute a PayU TEST transaction:

```powershell
$env:PAYU_TEST_ENABLED = "true"
$env:PAYU_TEST_KEY = "<your-test-key>"
$env:PAYU_TEST_SALT = "<your-test-salt>"
$env:PAYU_TEST_BASE_URL = "https://test.payu.in/_payment"
$env:PAYU_TEST_SUCCESS_URL = "http://localhost:8080/api/payments/providers/payu/callback"
$env:PAYU_TEST_FAILURE_URL = "http://localhost:8080/api/payments/providers/payu/callback"
```

Start or restart Spring Boot after changing configuration, then refresh the Angular page. The provider endpoint will return both `DUMMY` and `PAYU`, and the provider selector will show both options.

To hide PayU without removing the integration, set:

```yaml
payment:
	providers:
		payu:
			enabled: false
```

Never commit the real key or salt, put them in Angular environment files, or print them in logs.
