# PaymentJs-Java-Integration
Example Java integration of Payment.js

This implementation uses a SpringBoot environment.
The jar can be moved into whatever environement you wish.

## PaymentJS Documentation

https://docs.paymentjs.firstdata.com/

## Getting Started

Server set up is handled entirely in /src/main/resources/config.xml:

1) Add file path to payment-log (default location: /src/main/payment-log)

2) Select host (customer sandbox or production)

3) Add Payment.js credentials

4) Select gateway

5) Add gateway credentials

Current Spring Boot microservice url: http://localhost:7000

Server Port can be adjusted in application.properties.

## Webhook

The webhook endpoints is "/webhook." The webhook as configued in apigee should look like this:
https://{WEBHOOK_URL}/webhook

## Example config.xml:

<?xml version="1.0" encoding="UTF-8" ?>

<config>

    <log>
        <file_path>src/main/payment-log</file_path>
    </log>

    <host>

        <host_name>cert.api.firstdata.com</host_name>

        <!-- Customer sandbox -->
        <service_url>https://cert.api.firstdata.com/paymentjs/v2/merchant/authorize-session</service_url>

        <!--Production aka live
        <service_url>https://prod.api.firstdata.com/paymentjs/v2/merchant/authorize-session</service_url>
        -->

        <credentials>
            <api_key>ABCDEFGHIJKabcdefghijk1234567890</api_key>
            <api_secret>ABCabc1234567890</api_secret>
        </credentials>

    </host>

    <gateway>

        <current_gateway>payeezy</current_gateway>

        <payeezy>
            <gateway>PAYEEZY</gateway>
            <apiKey>LMNOPQRSTUVlmnopqrstuv1234567890</apiKey>
            <apiSecret>ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789000</apiSecret>
            <authToken>ABCDEFGHIJKLMNOPQRSTUVabcdefghijklmnopqrstuv1234567890</authToken>
            <transarmorToken>NOIW</transarmorToken>
            <zeroDollarAuth>TRUE</zeroDollarAuth>
        </payeezy>

        <bluepay>
            <gateway>BLUEPAY</gateway>
            <accountId>123456789000</accountId>
            <secretKey>LMNOPQRSTUVlmnopqrstuv1234567890</secretKey>
            <zeroDollarAuth>FALSE</zeroDollarAuth>
        </bluepay>

        <card_connect>
            <gateway>CARD_CONNECT</gateway>
            <apiUseRAndPass>test-username:test-password</apiUseRAndPass>
            <merchantId>123456789000</merchantId>
            <zeroDollarAuth>TRUE</zeroDollarAuth>
        </card_connect>

        <ipg>
            <gateway>IPG</gateway>
            <apiKey>LMNOPQRSTUVlmnopqrstuv1234567890</apiKey>
            <apiSecret>XYZxyz1234567890</apiSecret>
            <zeroDollarAuth>FALSE</zeroDollarAuth>
        </ipg>

    </gateway>

</config>
