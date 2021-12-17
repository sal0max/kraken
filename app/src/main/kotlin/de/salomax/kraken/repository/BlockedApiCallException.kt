package de.salomax.kraken.repository

class BlockedApiCallException : Exception(
    """Got redirected to 'https://www.instagram.com/accounts/login/'.
       Probably too many API calls from this IP. Wait some time and retry.
    """.trimMargin()
)
