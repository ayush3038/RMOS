# RMOS Explanation Layer (Milestone 3.12)

## Overview
The RMOS NVIDIA NIM Explanation Layer operates as a decoupled, isolated AI abstraction providing natural language reasoning and structural formatting via LLM inference over rigid system telemetry.

**This sub-service actively strictly limits operational mutation**:
- It **does not** override hardware capacity safety rules.
- It **does not** invoke CP-SAT optimizers or assign actual task schedules.
- It **does not** possess authorization features.

## Architecture

RMOS integrates with NVIDIA NIM securely referencing standard OpenAI Chat Completion contracts (targeting `/v1/chat/completions` directly) natively initialized out of `RequestMapping`.
**We deliberately eschew external SDK bounds (e.g., Python, LangChain, OpenAI libraries)** to maintain pristine localized Java bindings assuring absolute isolation.

### Service Layer Configurations
Configurations expose the inference bindings dynamically via toggleable environment limits:

```yaml
rmos:
  ai:
    nim:
      enabled: false # Enforces deterministic execution overrides when set locally.
      base-url: http://localhost:8000
      model: <target-model-name>
      api-key: [SECURE]
```

When disabled, `ExplanationService` fails gracefully—issuing deterministic localized mock explanation indicators bypassing remote execution seamlessly. 

### Context Input Builders
`ExplanationRequest` structures require `ExplanationContextType` mapping explicitly to one of the allowed categories:
- SAFETY_VALIDATION
- PRIORITY_ASSESSMENT
- ASSET_RISK
- PLANNING_RESULT
- SCENARIO_COMPARISON

### Model Validations & Strict Schemas
LLM execution responds within `json_object` configurations adhering cleanly to `ExplanationResponse`. We deliberately implement explicit Java validation logic parsing `ExplanationResponseValidator` verifying missing headers prior to yielding payload structures preventing unpredictable hallucinated arrays.

## Testing Constraints 
`NvidiaNimExplanationModelTest` utilizes MockMvc architectures confirming structural integration mappings independently without invoking active NIM pipelines offline.
`ExplanationArchitectureTest` enforces total prohibition of JPA/Operation packages preventing unintentional operational breaches guaranteeing stateless analytical bindings.
