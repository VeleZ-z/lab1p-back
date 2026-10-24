Feature: Banking API
  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Content-Type': 'application/json' }

  Scenario: Get all customers
    When method get
    Then status 200
    And match $.length > 0

  Scenario: Create transaction
    Given path '/api/transactions'
    And request { senderAccountNumber: 'ACC-001', receiverAccountNumber: 'ACC-002', amount: 250.0 }
    When method post
    Then status 200
    And match $.senderAccountNumber == 'ACC-001'
    And match $.receiverAccountNumber == 'ACC-002'
    And match $.amount == 250.0
