import { GoogleGenAI, Chat } from "@google/genai";
import { Project, Proposal } from "../types";

// Always initialize with the direct process.env.API_KEY in a named parameter object.
const ai = new GoogleGenAI({ apiKey: process.env.API_KEY });

/**
 * Generates a project description using a basic text model suitable for summarization tasks.
 */
export const generateProjectDescription = async (prompt: string): Promise<string> => {
  try {
    const response = await ai.models.generateContent({
      model: 'gemini-3-flash-preview',
      contents: `You are an expert architectural consultant. Write a professional, concise, and attractive project description for a homeowner looking for an architect.
      
      User rough idea: ${prompt}
      
      Keep it under 100 words. Focus on clarity and architectural terms.`,
    });
    // Property access .text directly extracts the response string.
    return response.text || "Unable to generate description.";
  } catch (error) {
    console.error("Error generating description:", error);
    return "Error connecting to AI service.";
  }
};

// --- Proposal Comparison Chat Features ---

/**
 * Initializes a comparison chat session using a pro model suitable for complex reasoning.
 */
export const createComparisonChat = (project: Project, proposals: Proposal[]): Chat | null => {
  const systemInstruction = `
    You are an objective architectural consultant assistant at Rumantra.
    Your job is to help the homeowner compare proposals for their project titled "${project.title}".
    
    Always answer in English.

    PROJECT REQUIREMENTS:
    - Description: ${project.description}
    - Budget: ${project.budget}
    - Requested Deliverables: ${JSON.stringify(project.deliverables || [])}

    PROPOSAL DATA:
    ${proposals.map((p, i) => `
    [${p.architectName}]
    - Bid Amount: ${p.bidAmount}
    - Duration: ${p.estimatedDuration}
    - Experience: ${p.architectExperience}
    - Rating: ${p.architectRating}
    - Features/Deliverables Included: ${JSON.stringify(p.features || [])}
    - Pitch: "${p.coverLetter}"
    `).join('\n')}

    RULES:
    1. NEVER recommend one architect over another. Your role is neutral analysis.
    
    2. **DELIVERABLES GAP ANALYSIS** (Priority for first response):
       For EACH architect, compare their 'Included Features' against the 'Requested Deliverables'.
       You MUST display this section in the following clear format:
       
       **[Architect Name]**
       *   ✅ **Matches**: [List items that match request]
       *   ❌ **Missing**: [List items requested but NOT found in proposal]
       *   ➕ **Extras**: [List value-add items not explicitly requested]

    3. **Value Synthesis**:
       After the deliverables list, provide a short 1-2 sentence summary of the trade-off (e.g., "Architect A is cheaper but misses X, while Architect B is complete but more expensive").

    4. **HANDLING FOLLOW-UP QUESTIONS**:
       The user might ask follow-ups after the initial analysis.
       - If asked about specific deliverables (e.g., "Does Architect A include MEP?"), ALWAYS cross-reference the "Included Features" list.
       - Provide side-by-side comparisons for specific questions.
       - Maintain context of project requirements.
       - Stay neutral and factual.

    5. Use clean Markdown formatting. Use lists and bold text for readability.
  `;

  try {
    return ai.chats.create({
      model: 'gemini-3-pro-preview',
      config: {
        systemInstruction: systemInstruction,
      },
    });
  } catch (error) {
    console.error("Error creating chat session:", error);
    return null;
  }
};

/**
 * Sends a message within an existing chat session.
 */
export const sendChatMessage = async (chat: Chat, message: string): Promise<string> => {
  try {
    const result = await chat.sendMessage({ message });
    return result.text || "I cannot generate a response.";
  } catch (error) {
    console.error("Error sending chat message:", error);
    return "Sorry, I'm having trouble connecting to the service right now.";
  }
};