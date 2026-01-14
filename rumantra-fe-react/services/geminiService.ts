import { GoogleGenAI, Type } from "@google/genai";
import { Proposal, Project, AnalysisResponse } from "../types";

const apiKey = process.env.API_KEY || '';
const ai = new GoogleGenAI({ apiKey });

// Helper to remove large Base64 fields before sending to AI
const sanitizeProposals = (proposals: Proposal[]) => {
  // We strip the PDF/Image Base64 strings to save tokens.
  // The AI analyzes the structured data (cost, duration, text descriptions) and extracted text.
  return proposals.map(({ pdfUrl, coverImage, facadeImage, interiorImages, layoutImages, ...rest }) => rest);
};

export const analyzeProposals = async (
  project: Project,
  proposals: Proposal[]
): Promise<AnalysisResponse> => {
  if (!apiKey) {
    throw new Error("API Key is missing");
  }

  const model = "gemini-2.5-flash";

  // We strip the PDF Base64 string to save tokens. 
  // The AI analyzes the structured data (cost, duration, text descriptions).
  const sanitizedProposals = sanitizeProposals(proposals);

  const prompt = `
    You are an expert Architectural Consultant for Rumantra, a platform connecting homeowners with architects in Indonesia. 
    Analyze the following proposals for a client's project.
    
    Project Details:
    Title: ${project.title}
    Location: ${project.location}
    Type: ${project.buildingType}
    Lot Size: ${project.lotSize} m2
    Description: ${project.description}
    Total Construction Budget: ${project.totalBudget || 'N/A'}
    Design Budget (Target): ${project.designBudget || project.budget}
    Requested Deliverables: ${project.deliverables ? project.deliverables.join(', ') : 'Not specified'}

    Proposals:
    ${JSON.stringify(sanitizedProposals)}

    Task:
    1. Compare the proposals based on Cost, Duration, and Design Concept suitability.
    2. Check if the architect likely covers the requested deliverables (e.g. IMB requirements, DED) based on their description/extracted text.
    3. Assign a score (0-100) for Cost (Higher score means better/cheaper), Time (Higher score means faster), and Design (Higher means fits description better).
    4. Provide a brief summary, pros, and cons for each.
    5. Provide a final recommendation on who to choose and why.
    6. Provide a "topOptionsSummary" which is a concise, high-level summary comparing the top 2-3 proposals for a quick decision (e.g., "Architect A is the most affordable choice, while Architect B offers a faster timeline...").
  `;

  try {
    const response = await ai.models.generateContent({
      model,
      contents: prompt,
      config: {
        responseMimeType: "application/json",
        responseSchema: {
          type: Type.OBJECT,
          properties: {
            comparison: {
              type: Type.ARRAY,
              items: {
                type: Type.OBJECT,
                properties: {
                  architectName: { type: Type.STRING },
                  costScore: { type: Type.NUMBER },
                  timeScore: { type: Type.NUMBER },
                  designScore: { type: Type.NUMBER },
                  summary: { type: Type.STRING },
                  pros: { type: Type.ARRAY, items: { type: Type.STRING } },
                  cons: { type: Type.ARRAY, items: { type: Type.STRING } },
                },
                required: ["architectName", "costScore", "timeScore", "designScore", "summary", "pros", "cons"]
              }
            },
            recommendation: { type: Type.STRING },
            topOptionsSummary: { type: Type.STRING }
          },
          required: ["comparison", "recommendation", "topOptionsSummary"]
        }
      }
    });

    const resultText = response.text;
    if (!resultText) throw new Error("No response from AI");
    
    return JSON.parse(resultText) as AnalysisResponse;

  } catch (error) {
    console.error("Error analyzing proposals:", error);
    throw error;
  }
};

export const chatWithData = async (
  history: { role: 'user' | 'model', text: string }[],
  project: Project,
  proposals: Proposal[],
  newMessage: string
) => {
    if (!apiKey) throw new Error("API Key missing");
    
    const sanitizedProposals = sanitizeProposals(proposals);

    const context = `
      Context: User is a client on Rumantra asking about proposals for project "${project.title}".
      Project Deliverables: ${project.deliverables ? project.deliverables.join(', ') : 'Not specified'}
      Proposals data: ${JSON.stringify(sanitizedProposals)}.
      Answer the user's question specifically comparing the available proposals.
    `;

    const chat = ai.chats.create({
      model: 'gemini-2.5-flash',
      config: {
        systemInstruction: context
      },
      history: history.map(h => ({
        role: h.role,
        parts: [{ text: h.text }]
      }))
    });

    const result = await chat.sendMessage({ message: newMessage });
    return result.text;
};