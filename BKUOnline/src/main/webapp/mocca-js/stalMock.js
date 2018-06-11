define('stalMock', function () {
    
    var demoCertBase64 = "MIIEFTCCAv2gAwIBAgIJAMtFZnr7TIzkMA0GCSqGSIb3DQEBBQUAMGQxCzAJBgNV"+
    "BAYTAkFUMRMwEQYDVQQIEwpTb21lLVN0YXRlMQ0wCwYDVQQHEwRHcmF6MRcwFQYD"+
    "VQQKEw5QcmltZVNpZ24gR21iSDEYMBYGA1UEAxMPREVNTyBaRVJUSUZJS0FUMB4X"+
    "DTE4MDUwODEzMDAzNFoXDTIwMDUwNzEzMDAzNFowZDELMAkGA1UEBhMCQVQxEzAR"+
    "BgNVBAgTClNvbWUtU3RhdGUxDTALBgNVBAcTBEdyYXoxFzAVBgNVBAoTDlByaW1l"+
    "U2lnbiBHbWJIMRgwFgYDVQQDEw9ERU1PIFpFUlRJRklLQVQwggEiMA0GCSqGSIb3"+
    "DQEBAQUAA4IBDwAwggEKAoIBAQDcpCJ/y+UeDI9XwzDcFXUxgYBrMvNC0OymGUnV"+
    "ue+jzsQ43PQ2h0wlvJbzyKOLHJUk+koN6WrfecmBgKIoc/ZI5IKdAf4GLVLsMJy1"+
    "0O/SFfpnHID42Io4C7WAMJ1PKPlShZlC/LPPkGAChJsxZNeBKzv9Axtf3636ykIb"+
    "gSYMjZdHPMeJnVBzS5NTsxU4ixamj9lslS5m4XZcPu4c0DC/9rVIEGK0DM4wylb2"+
    "dCQ4xi/wMtGpZoDHr3jt1JtYjLDrmactdhPAiYYczN+kjEc3k2sUbyvNwmVauagi"+
    "3kbCGH+Y+lsUrlwnFfkGnIwNIw3zkgrjZZnocJGGfq+cF2rPAgMBAAGjgckwgcYw"+
    "HQYDVR0OBBYEFNNjRt03b9uqJr0QTSGXdOm0XjIFMIGWBgNVHSMEgY4wgYuAFNNj"+
    "Rt03b9uqJr0QTSGXdOm0XjIFoWikZjBkMQswCQYDVQQGEwJBVDETMBEGA1UECBMK"+
    "U29tZS1TdGF0ZTENMAsGA1UEBxMER3JhejEXMBUGA1UEChMOUHJpbWVTaWduIEdt"+
    "YkgxGDAWBgNVBAMTD0RFTU8gWkVSVElGSUtBVIIJAMtFZnr7TIzkMAwGA1UdEwQF"+
    "MAMBAf8wDQYJKoZIhvcNAQEFBQADggEBAEfQhpUq81vyNyohqW48+D1te5JnFQnX"+
    "6We9O2cVy117P4a9rTuPRt7+Q3icxbV5bhgg57TgBtrz9Z/GRBiYDZpIe+DcN7zl"+
    "kxYJxc7B06xia+3NZL0bEDTjFuuIOpUM59vZASyQQDLWhploDWcXALrqrmpUqHJj"+
    "F38fpGLSEWKPAX+jhutRLHCR1TjNpBuxYWccvnKX/uRRvrTzTuXX4KLXZ+CmwbyI"+
    "LR8sr/Ed92T6+TMzwOkcCbD3FqkBme/cqAnQglUauhcD/TMVXrGQ0t4wn7HHubVC"+
    "Z3A1/PSKQzBcU8k6Lw8fI93FaBowiCvHraPVuM+ZFjq16rk7A04YjrU=";

    return {
        selectCertificate: function() {
            return demoCertBase64;
        },
        sign: function (certificate, algorithmId, dataToBeSigned) {
            // TODO convert dataToBeSigned to signedData
            return dataToBeSigned;
        }
    };
});