## 2026-03-15 - LLM Prompt Injection Tag Breakout in Prompt Templates
**Vulnerability:** Untrusted user code passed to LLM model prompt templates could contain closing XML tags (`</user_code>`) to close the delimiter block early and supply malicious instructions.
**Learning:** Even when wrapping untrusted user input inside XML tags, models can be tricked if closing tags inside the user input are not escaped before string interpolation into the prompt template.
**Prevention:** Sanitize or escape any closing tag occurrences (e.g. `</user_code>` -> `</user_code_escaped>`) in untrusted strings before embedding them into XML-delimited prompt templates.
