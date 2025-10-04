# Underfried Multi-Agent System - Message Formats

## System Overview

The Underfried kitchen simulation uses a multi-agent system with four main agents communicating through JADE ACL messages. Based on the communication diagram, here are all the message formats and communication patterns.

## Message Formats by Agent

### **Garçom (Waiter) Agent**

#### **Receives:**

- TODO: Define waiter message formats in future

#### **Sends:**

TODO: Define waiter message formats in future

### **Chef Agent**

#### **Receives:**

**1. Orders from Waiter**

TODO: Define waiter message formats in future

#### **Sends:**

**1. Ingredient Ready Notification to DishPreparer**

```
Message Type: ACLMessage.INFORM
Recipient: AID("dishPreparer", AID.ISLOCALNAME)
Format: "INGREDIENT_READY:STATUS:INGREDIENT:MEAL"
Status Values:
  - COOKED: Ingredient has been cooked
  - CUT: Ingredient has been cut/chopped
  - CUT_AND_COOKED: Ingredient has been both cut and cooked
  - RAW: Ingredient is used raw (no processing needed)

Examples:
  - "INGREDIENT_READY:COOKED:meat:super_meat_boy"
  - "INGREDIENT_READY:CUT:tomato:salad"
  - "INGREDIENT_READY:CUT_AND_COOKED:chicken:super_chicken_boy"
  - "INGREDIENT_READY:RAW:lettuce:salad"
```

**2. Error Response to Waiter**

```
Message Type: ACLMessage.FAILURE
Recipient: Original sender (Waiter)
Format: "CHEF_ERROR:ERROR_MESSAGE"
Examples:
  - "CHEF_ERROR:Empty order content"
  - "CHEF_ERROR:Unknown meal: invalid_dish"
  - "CHEF_ERROR:Processing error: [details]"
```

---

### **Preparador de Pratos (DishPreparer) Agent**

#### **Receives:**

**1. Ingredient Ready from Chef**

```
Message Type: ACLMessage.INFORM
Format: "INGREDIENT_READY:STATUS:INGREDIENT:MEAL"
Processing: Tracks ingredients per meal until all are ready
```

**2. Clean Plates from Dishwasher**

```
Message Type: ACLMessage.INFORM
Format: "CLEAN_PLATES:COUNT"
Example: "CLEAN_PLATES:5"
Processing: Updates restaurant.cleanPlates count
```

#### **Sends:**

- **No outgoing messages** (as per diagram - waiter checks ready dishes independently)
- Updates `restaurant.readyDishes` queue directly when dishes are completed

---

### **Lavador de Louças (Dishwasher) Agent** _(To be implemented)_

#### **Receives:**

**1. Dirty Plates from Waiter**

TODO: Define waiter message formats in future

#### **Sends:**

**1. Clean Plates to DishPreparer**

TODO: Define dishwasher message formats in future

---

## **Implementation Status**

- **Chef**: Fully implemented with all message formats
- **DishPreparer**: Fully implemented with all message formats
- **Waiter**: Partially implemented
- **Dishwasher**: Not yet implemented

```

```
