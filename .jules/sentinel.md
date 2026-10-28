## 2026-09-16 - Prompt Injection XML Tag Escaping in LLM Prompts
**Vulnerability:** Prompt injection via closing delimiting tags (`</user_code>`) allowed user-controlled code blocks to break out of data boundaries and inject system-level instructions into the LLM context.
**Learning:** System prompts that rely on pseudo-XML tags for data/instruction separation require strict escaping of closing tags in user-supplied content before prompt construction.
**Prevention:** Always sanitize or escape delimiting tag sequences (e.g., `</user_code>` -> `<\/user_code>`) in user inputs embedded within structured LLM prompts.
