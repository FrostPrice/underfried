# Underfried Multi-Agent System - Message Formats

## System Overview

The Underfried kitchen simulation uses a multi-agent system with four main agents communicating through JADE ACL messages. Based on the communication diagram, here are all the message formats and communication patterns.

## Message Formats by Agent

### **Garçom (Waiter) Agent**

#### **Receives:**

- **No incoming messages** (Waiter acts independently based on restaurant state)

#### **Sends:**

**1. Orders to Chef**

```
Message Type: ACLMessage.INFORM
Recipient: AID("chef", AID.ISLOCALNAME)
Format: "DISH1\nDISH2\n..." (multiple dishes separated by newlines)
Examples:
  - "super_meat_boy"
  - "salad\nsuper_meat_boy\npasta"
Processing:
  - Randomly selects dishes from available menu
  - Adds orders to restaurant.pendingOrders queue
  - Sends all collected orders in single message
```

**2. Dirty Plates Notification to DishWasher**

```
Message Type: ACLMessage.INFORM
Recipient: AID("dishWasher", AID.ISLOCALNAME)
Format: "DIRTY_PLATES:COUNT"
Examples:
  - "DIRTY_PLATES:3"
  - "DIRTY_PLATES:5"
Processing:
  - Updates restaurant.dirtyPlates count before sending
  - Collected from dining area tables
```

### **Chef Agent**

#### **Receives:**

**1. Orders from Waiter**

```
Message Type: ACLMessage.INFORM
Sender: Waiter agent
Format: "DISH1\nDISH2\n..." (multiple dishes separated by newlines)
Examples:
  - "super_meat_boy"
  - "salad\nsuper_meat_boy\npasta"
Processing:
  - Validates each order against restaurant menu
  - Cross-references with restaurant.pendingOrders queue
  - Processes each meal sequentially
```

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

### **Lavador de Louças (Dishwasher) Agent**

#### **Receives:**

**1. Dirty Plates Notification from Waiter**

```text
Message Type: ACLMessage.INFORM
Sender: Waiter agent
Format: "DIRTY_PLATES:COUNT"
Examples:
  - "DIRTY_PLATES:3"
  - "DIRTY_PLATES:5"
Processing:
  - Acknowledges notification of dirty plates
  - restaurant.dirtyPlates already updated by waiter
  - Triggers washing behavior
```

#### **Sends:**

**1. Clean Plates to DishPreparer**

```text
Message Type: ACLMessage.INFORM
Recipient: AID("dishPreparer", AID.ISLOCALNAME)
Format: "CLEAN_PLATES:COUNT"
Examples:
  - "CLEAN_PLATES:3"
  - "CLEAN_PLATES:5"
Processing:
  - Sent after washing batch is complete
  - Each plate takes 2 seconds to wash
  - Maximum 5 plates per batch
  - DishPreparer updates restaurant.cleanPlates count
```

---

## **Communication Flow Summary**

```
Waiter → Chef: Orders (meal names)
Waiter → DishWasher: Dirty plates notification

Chef → DishPreparer: Ingredient ready notifications (with status)

DishWasher → DishPreparer: Clean plates count

DishPreparer: Updates restaurant.readyDishes queue (checked by Waiter)
```

## **Shared State Management**

The agents communicate both through messages and shared state via the `Restaurant` object:

- **restaurant.pendingOrders**: Queue of orders (Waiter adds, Chef removes)
- **restaurant.readyDishes**: Queue of completed dishes (DishPreparer adds, Waiter removes)
- **restaurant.dirtyPlates**: Count of dirty plates (Waiter adds, DishWasher removes)
- **restaurant.cleanPlates**: Count of clean plates (DishWasher adds via message to DishPreparer)
- **restaurant.takenPlates**: Plates currently with customers (Waiter manages)

## **Environmental Conditions Handled**

- **FIRE**: Detected and extinguished by Chef
- **RAT**: Detected and eliminated ("bonked") by Waiter
- **BURNED_FOOD**: Detected and discarded by Chef at cooking station
