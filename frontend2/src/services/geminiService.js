import { GoogleGenAI, Type } from '@google/genai'

const apiKey = import.meta.env.VITE_GEMINI_API_KEY || ''
const ai = new GoogleGenAI({ apiKey })

const sanitizeProposals = proposals => {
  return proposals.map(
    ({ pdfUrl, coverImage, facadeImage, interiorImages, layoutImages, ...rest }) => rest
  )
}

export const analyzeProposals = async (project, proposals) => {
  if (!apiKey) {
    throw new Error(
      'Gemini API Key is missing. Please set VITE_GEMINI_API_KEY in your .env.local file'
    )
  }

  const model = 'gemini-2.5-flash'
  const sanitizedProposals = sanitizeProposals(proposals)

  const prompt = `
    You are an expert Architectural Consultant for Rumantra, a platform connecting homeowners with architects in Indonesia.
    Analyze the following proposals for a client's project.

    Project Details:
    Title: ${project.title}
    Location: ${project.location}
    Scope: ${project.projectScope || 'N/A'}
    Category: ${project.buildingFunction || project.buildingType}
    Sub-Category: ${project.subCategory || 'N/A'}
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
  `

  try {
    const response = await ai.models.generateContent({
      model,
      contents: prompt,
      config: {
        responseMimeType: 'application/json',
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
                  overallScore: { type: Type.NUMBER },
                  summary: { type: Type.STRING },
                  pros: { type: Type.ARRAY, items: { type: Type.STRING } },
                  cons: { type: Type.ARRAY, items: { type: Type.STRING } }
                },
                required: [
                  'architectName',
                  'costScore',
                  'timeScore',
                  'designScore',
                  'overallScore',
                  'summary',
                  'pros',
                  'cons'
                ]
              }
            },
            recommendation: { type: Type.STRING },
            topOptionsSummary: { type: Type.STRING }
          },
          required: ['comparison', 'recommendation', 'topOptionsSummary']
        }
      }
    })

    const resultText = response.text
    if (!resultText) throw new Error('No response from AI')

    return JSON.parse(resultText)
  } catch (error) {
    console.error('Error analyzing proposals:', error)
    throw error
  }
}

export const chatWithData = async (history, project, proposals, newMessage) => {
  if (!apiKey) throw new Error('Gemini API Key is missing')

  const sanitizedProposals = sanitizeProposals(proposals)

  const context = `
    Context: User is a client on Rumantra asking about proposals for project "${project.title}".
    Project Deliverables: ${project.deliverables ? project.deliverables.join(', ') : 'Not specified'}
    Proposals data: ${JSON.stringify(sanitizedProposals)}.
    Answer the user's question specifically comparing the available proposals.
  `

  const chat = ai.chats.create({
    model: 'gemini-2.5-flash',
    config: {
      systemInstruction: context
    },
    history: history.map(h => ({
      role: h.role,
      parts: [{ text: h.text }]
    }))
  })

  const result = await chat.sendMessage({ message: newMessage })
  return result.text
}

export const polishPhilosophy = async text => {
  if (!apiKey) {
    console.warn('Gemini API key not configured')
    return text
  }

  try {
    const model = 'gemini-2.5-flash'
    const prompt = `You are an editorial assistant for architects. Rewrite this architectural philosophy to be more elegant and professional while preserving the core meaning. Keep it concise (2-3 sentences maximum). Use sophisticated but accessible language.

Original philosophy: "${text}"

Rewrite:`

    const response = await ai.models.generateContent({
      model,
      contents: prompt,
      config: {
        temperature: 0.7,
        maxOutputTokens: 200
      }
    })

    const polishedText = response.text

    if (!polishedText) {
      throw new Error('No response from API')
    }

    return polishedText.trim()
  } catch (error) {
    console.error('Philosophy polishing error:', error)
    return text
  }
}

export const getArchitecturalAdvice = async context => {
  if (!apiKey) {
    return 'Continue crafting your architectural narrative.'
  }

  const prompts = {
    IDENTITY: 'Share advice about establishing a professional architectural practice identity.',
    PHILOSOPHY: 'Share a brief insight about developing a strong architectural design philosophy.',
    EXPERTISE: 'Share advice about specializing in architectural domains.',
    PORTFOLIO: 'Share advice about showcasing architectural projects effectively.',
    default: 'Share a brief architectural design insight.'
  }

  const prompt = prompts[context] || prompts.default

  try {
    const model = 'gemini-2.5-flash'
    const response = await ai.models.generateContent({
      model,
      contents: `${prompt} Respond in one elegant sentence. Be inspiring but concise.`,
      config: {
        temperature: 0.8,
        maxOutputTokens: 100
      }
    })

    const advice = response.text

    return advice?.trim() || 'Continue crafting your architectural narrative.'
  } catch (error) {
    console.error('AI advice error:', error)
    return 'Continue crafting your architectural narrative.'
  }
}
