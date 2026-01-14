import React, { useState, useEffect } from 'react';
import { Proposal } from '../types';
import { Send, Clock, DollarSign, PenTool, Layers, UploadCloud, FileText, X, Image as ImageIcon, Eye, Maximize2, File, Home, Grid, Armchair, Plus } from 'lucide-react';
import * as pdfjsLib from 'pdfjs-dist';

// Defensive import for PDF.js to handle various ESM/CommonJS build formats
const pdfjs = (pdfjsLib as any).default || pdfjsLib;

// Initialize PDF.js worker
if (pdfjs.GlobalWorkerOptions) {
  pdfjs.GlobalWorkerOptions.workerSrc = 'https://aistudiocdn.com/pdfjs-dist@3.11.174/build/pdf.worker.min.mjs';
}

interface Props {
  projectId: string;
  onSubmit: (proposal: Proposal) => void;
  onCancel: () => void;
}

// Reusable Single Image Uploader Component
const ImageUploader: React.FC<{
  label: string;
  file: File | null;
  setFile: (file: File | null) => void;
  icon?: React.ElementType;
}> = ({ label, file, setFile, icon: Icon = ImageIcon }) => {
  const [isDragging, setIsDragging] = useState(false);

  const validateImage = (file: File) => {
    const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
    if (!validTypes.includes(file.type)) {
      alert("Only JPG, PNG, and GIF files are allowed.");
      return false;
    }
    if (file.size > 5 * 1024 * 1024) { // 5MB limit
      alert("File size exceeds the 5MB limit.");
      return false;
    }
    return true;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const f = e.target.files[0];
      if (validateImage(f)) setFile(f);
      else e.target.value = '';
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const f = e.dataTransfer.files[0];
      if (validateImage(f)) setFile(f);
    }
  };

  const inputId = `file-upload-${label.replace(/\s+/g, '-').toLowerCase()}`;

  return (
    <div className="h-full flex flex-col">
      <label className="block text-xs font-semibold text-slate-500 uppercase mb-2">{label}</label>
      {!file ? (
        <div 
          onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
          onDragLeave={(e) => { e.preventDefault(); setIsDragging(false); }}
          onDrop={handleDrop}
          className={`flex-1 min-h-[160px] border-2 border-dashed rounded-lg flex flex-col items-center justify-center transition-all cursor-pointer ${
            isDragging ? 'border-[#1B4D89] bg-blue-50' : 'border-slate-300 hover:border-[#1B4D89] hover:bg-slate-50'
          }`}
        >
          <input type="file" accept="image/jpeg, image/png, image/gif" onChange={handleChange} className="hidden" id={inputId} />
          <label htmlFor={inputId} className="flex flex-col items-center cursor-pointer w-full h-full justify-center">
            <Icon className={`w-6 h-6 mb-2 ${isDragging ? 'text-[#1B4D89]' : 'text-slate-400'}`} />
            <span className="text-xs text-slate-600">Upload {label}</span>
          </label>
        </div>
      ) : (
        <div className="relative flex-1 min-h-[160px] rounded-lg overflow-hidden border border-slate-200 group">
          <img src={URL.createObjectURL(file)} alt={label} className="w-full h-full object-cover" />
          <button 
            type="button"
            onClick={() => setFile(null)}
            className="absolute top-2 right-2 p-1 bg-white/90 rounded-full text-slate-600 hover:text-red-500 shadow-sm"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      )}
    </div>
  );
};

// Reusable Multi Image Uploader Component
const MultiImageUploader: React.FC<{
  label: string;
  files: File[];
  setFiles: (files: File[]) => void;
  icon?: React.ElementType;
}> = ({ label, files, setFiles, icon: Icon = ImageIcon }) => {
  const [isDragging, setIsDragging] = useState(false);

  const validateImage = (file: File) => {
    const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
    if (!validTypes.includes(file.type)) return false;
    if (file.size > 5 * 1024 * 1024) return false;
    return true;
  };

  const handleFiles = (newFiles: FileList | null) => {
    if (!newFiles) return;
    const validFiles: File[] = [];
    Array.from(newFiles).forEach(f => {
      if (validateImage(f)) validFiles.push(f);
    });
    setFiles([...files, ...validFiles]);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    handleFiles(e.target.files);
    e.target.value = ''; // Reset to allow same file selection again
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    handleFiles(e.dataTransfer.files);
  };

  const removeFile = (index: number) => {
    setFiles(files.filter((_, i) => i !== index));
  };

  const inputId = `multi-file-upload-${label.replace(/\s+/g, '-').toLowerCase()}`;

  return (
    <div className="h-full flex flex-col">
      <label className="block text-xs font-semibold text-slate-500 uppercase mb-2">
        {label} <span className="text-[10px] font-normal text-slate-400">(Multi)</span>
      </label>
      
      {files.length === 0 ? (
        // Empty State - matches Single Uploader size
        <div 
          onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
          onDragLeave={(e) => { e.preventDefault(); setIsDragging(false); }}
          onDrop={handleDrop}
          className={`flex-1 min-h-[160px] border-2 border-dashed rounded-lg flex flex-col items-center justify-center transition-all cursor-pointer ${
            isDragging ? 'border-[#1B4D89] bg-blue-50' : 'border-slate-300 hover:border-[#1B4D89] hover:bg-slate-50'
          }`}
        >
          <input 
            type="file" 
            accept="image/jpeg, image/png, image/gif" 
            multiple 
            onChange={handleChange} 
            className="hidden" 
            id={inputId} 
          />
          <label htmlFor={inputId} className="flex flex-col items-center cursor-pointer w-full h-full justify-center">
            <Icon className={`w-6 h-6 mb-2 ${isDragging ? 'text-[#1B4D89]' : 'text-slate-400'}`} />
            <span className="text-xs text-slate-600 text-center">Upload {label}</span>
            <span className="text-[10px] text-slate-400 mt-1">Drag & drop multiple files</span>
          </label>
        </div>
      ) : (
        <div className="flex-1 min-h-[160px] bg-slate-50 border border-slate-200 rounded-lg p-2">
           <div className="grid grid-cols-2 gap-2 h-full content-start">
              {/* Upload Button Small */}
              <div className="aspect-square border-2 border-dashed border-slate-300 rounded-lg flex items-center justify-center hover:border-[#1B4D89] hover:bg-blue-50 cursor-pointer relative">
                <input 
                    type="file" 
                    accept="image/jpeg, image/png, image/gif" 
                    multiple 
                    onChange={handleChange} 
                    className="absolute inset-0 opacity-0 cursor-pointer" 
                  />
                  <Plus className="w-5 h-5 text-slate-400" />
              </div>

              {/* Previews */}
              {files.map((file, index) => (
                <div key={index} className="aspect-square relative rounded-lg overflow-hidden border border-slate-200 group bg-white">
                  <img src={URL.createObjectURL(file)} alt={`Preview ${index}`} className="w-full h-full object-cover" />
                  <button 
                    type="button"
                    onClick={() => removeFile(index)}
                    className="absolute top-1 right-1 p-0.5 bg-white/90 rounded-full text-slate-600 hover:text-red-500 shadow-sm opacity-0 group-hover:opacity-100 transition-opacity"
                  >
                    <X className="w-3 h-3" />
                  </button>
                </div>
              ))}
           </div>
        </div>
      )}
    </div>
  );
};

const ProposalForm: React.FC<Props> = ({ projectId, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    architectName: '',
    firmName: '',
    estimatedCost: '',
    durationMonths: '',
    conceptDescription: '',
    materialsStrategy: ''
  });
  
  const [pdfFile, setPdfFile] = useState<File | null>(null);
  const [pdfThumbnail, setPdfThumbnail] = useState<string | null>(null);
  const [pdfPreviewUrl, setPdfPreviewUrl] = useState<string | null>(null);
  const [pdfPageCount, setPdfPageCount] = useState<number>(0);
  const [showPdfModal, setShowPdfModal] = useState(false);

  // Single Image State
  const [coverImageFile, setCoverImageFile] = useState<File | null>(null);
  const [facadeImageFile, setFacadeImageFile] = useState<File | null>(null);

  // Multi Image State
  const [interiorFiles, setInteriorFiles] = useState<File[]>([]);
  const [layoutFiles, setLayoutFiles] = useState<File[]>([]);

  const [isDraggingPdf, setIsDraggingPdf] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Cleanup object URL to prevent memory leaks
  useEffect(() => {
    return () => {
      if (pdfPreviewUrl) {
        URL.revokeObjectURL(pdfPreviewUrl);
      }
    };
  }, [pdfPreviewUrl]);

  const convertToBase64 = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = error => reject(error);
    });
  };

  const extractTextFromPDF = async (file: File): Promise<string> => {
    try {
      if (!pdfjs) return "";
      const arrayBuffer = await file.arrayBuffer();
      const pdf = await pdfjs.getDocument({ data: arrayBuffer }).promise;
      let fullText = '';
      
      const maxPages = Math.min(pdf.numPages, 15);
      
      for (let i = 1; i <= maxPages; i++) {
        const page = await pdf.getPage(i);
        const textContent = await page.getTextContent();
        const pageText = textContent.items.map((item: any) => item.str).join(' ');
        fullText += `[Page ${i}] ${pageText}\n`;
      }
      return fullText;
    } catch (error) {
      console.error("Error extracting text from PDF:", error);
      return "";
    }
  };

  const generateThumbnailAndMetadata = async (file: File): Promise<{ thumbnail: string | null, pages: number }> => {
    try {
      if (!pdfjs) return { thumbnail: null, pages: 0 };
      const arrayBuffer = await file.arrayBuffer();
      const pdf = await pdfjs.getDocument({ data: arrayBuffer }).promise;
      const page = await pdf.getPage(1);
      const pages = pdf.numPages;
      
      // Render a small thumbnail
      const viewport = page.getViewport({ scale: 0.5 });
      const canvas = document.createElement('canvas');
      const context = canvas.getContext('2d');
      canvas.height = viewport.height;
      canvas.width = viewport.width;

      let thumbnail = null;
      if (context) {
        await page.render({ canvasContext: context, viewport }).promise;
        thumbnail = canvas.toDataURL();
      }
      return { thumbnail, pages };
    } catch (error) {
      console.error("Thumbnail generation failed", error);
      return { thumbnail: null, pages: 0 };
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    
    try {
      let pdfUrl = undefined;
      let pdfExtractedText = undefined;
      if (pdfFile) {
        try {
          pdfUrl = await convertToBase64(pdfFile);
          pdfExtractedText = await extractTextFromPDF(pdfFile);
        } catch (error) {
          console.error("Error reading PDF file", error);
          alert("Failed to process PDF file");
          setIsSubmitting(false);
          return;
        }
      }

      const processFile = async (file: File | null) => {
        if (!file) return undefined;
        try {
          return await convertToBase64(file);
        } catch (error) {
          console.error("Error reading image file", error);
          return undefined;
        }
      };

      const processMultipleFiles = async (files: File[]) => {
        try {
          const promises = files.map(file => convertToBase64(file));
          return await Promise.all(promises);
        } catch (error) {
          console.error("Error processing multiple images", error);
          return [];
        }
      };

      const coverImageUrl = await processFile(coverImageFile);
      const facadeImageUrl = await processFile(facadeImageFile);
      
      const interiorImages = await processMultipleFiles(interiorFiles);
      const layoutImages = await processMultipleFiles(layoutFiles);

      if (coverImageFile && !coverImageUrl) {
         alert("Failed to process cover image");
         setIsSubmitting(false);
         return;
      }

      const proposal: Proposal = {
        id: crypto.randomUUID(),
        projectId,
        architectName: formData.architectName,
        firmName: formData.firmName,
        estimatedCost: Number(formData.estimatedCost),
        durationMonths: Number(formData.durationMonths),
        conceptDescription: formData.conceptDescription,
        materialsStrategy: formData.materialsStrategy,
        submittedAt: new Date().toISOString(),
        pdfUrl: pdfUrl,
        pdfFileName: pdfFile?.name,
        pdfExtractedText: pdfExtractedText,
        coverImage: coverImageUrl,
        facadeImage: facadeImageUrl,
        interiorImages: interiorImages,
        layoutImages: layoutImages
      };
      onSubmit(proposal);
    } catch (error) {
      console.error("Submission error:", error);
      alert("An error occurred while submitting the proposal.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const validatePdf = (file: File) => {
    if (file.type !== "application/pdf") {
      alert("Only PDF files are allowed.");
      return false;
    }
    if (file.size > 10 * 1024 * 1024) { // 10MB limit
      alert("File size exceeds the 10MB limit.");
      return false;
    }
    return true;
  };

  const processPdfFile = async (file: File) => {
    setPdfFile(file);
    // Create Object URL for preview modal
    const url = URL.createObjectURL(file);
    setPdfPreviewUrl(url);
    
    // Generate visual thumbnail
    const { thumbnail, pages } = await generateThumbnailAndMetadata(file);
    setPdfThumbnail(thumbnail);
    setPdfPageCount(pages);
  };

  // --- PDF Handlers ---
  const handlePdfChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      if (validatePdf(file)) {
        processPdfFile(file);
      } else {
        e.target.value = '';
      }
    }
  };
  const handlePdfDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDraggingPdf(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const file = e.dataTransfer.files[0];
      if (validatePdf(file)) {
        processPdfFile(file);
      }
    }
  };
  const removePdf = () => {
    setPdfFile(null);
    setPdfThumbnail(null);
    if (pdfPreviewUrl) URL.revokeObjectURL(pdfPreviewUrl);
    setPdfPreviewUrl(null);
    setPdfPageCount(0);
  };

  return (
    <>
      <div className="bg-white rounded-xl shadow-lg border border-slate-200 p-6 animate-fade-in">
        <h3 className="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2 font-['Barlow']">
          <PenTool className="w-5 h-5 text-[#FD5E53]" />
          Submit Proposal
        </h3>
        
        <form onSubmit={handleSubmit} className="space-y-8">
          {/* Basic Info */}
          <div className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-500 uppercase mb-1">Architect Name</label>
                <input required type="text" name="architectName" value={formData.architectName} onChange={handleChange} className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:border-[#1B4D89] focus:ring-0 outline-none" />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-500 uppercase mb-1">Firm Name</label>
                <input type="text" name="firmName" value={formData.firmName} onChange={handleChange} className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:border-[#1B4D89] focus:ring-0 outline-none" />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-500 uppercase mb-1">Total Est. Cost (IDR)</label>
                <div className="relative">
                  <input required type="number" name="estimatedCost" value={formData.estimatedCost} onChange={handleChange} className="w-full pl-8 px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:border-[#1B4D89] focus:ring-0 outline-none" placeholder="e.g., 500000000" />
                  <DollarSign className="w-4 h-4 text-slate-400 absolute left-2 top-2.5" />
                </div>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-500 uppercase mb-1">Duration (Months)</label>
                <div className="relative">
                  <input required type="number" name="durationMonths" value={formData.durationMonths} onChange={handleChange} className="w-full pl-8 px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:border-[#1B4D89] focus:ring-0 outline-none" placeholder="e.g., 6" />
                  <Clock className="w-4 h-4 text-slate-400 absolute left-2 top-2.5" />
                </div>
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-500 uppercase mb-1">Design Concept & Vision</label>
              <textarea required name="conceptDescription" value={formData.conceptDescription} onChange={handleChange} rows={4} className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:border-[#1B4D89] focus:ring-0 outline-none resize-none" placeholder="Describe your architectural approach, style, and unique value proposition..." />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-500 uppercase mb-1">Materials & Sustainability Strategy</label>
              <div className="relative">
                <input type="text" name="materialsStrategy" value={formData.materialsStrategy} onChange={handleChange} className="w-full pl-8 px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:border-[#1B4D89] focus:ring-0 outline-none" placeholder="e.g., Locally sourced bamboo, Solar passive design" />
                <Layers className="w-4 h-4 text-slate-400 absolute left-2 top-2.5" />
              </div>
            </div>
          </div>

          <div className="space-y-6 pt-2 border-t border-slate-100">
             {/* PDF Upload - Moved Above Images */}
             <div>
              <label className="block text-xs font-semibold text-slate-500 uppercase mb-2">Detailed Proposal (PDF)</label>
              {!pdfFile ? (
                <div 
                  onDragOver={(e) => { e.preventDefault(); setIsDraggingPdf(true); }}
                  onDragLeave={(e) => { e.preventDefault(); setIsDraggingPdf(false); }}
                  onDrop={handlePdfDrop}
                  className={`border-2 border-dashed rounded-lg h-32 flex flex-col items-center justify-center transition-all cursor-pointer ${
                    isDraggingPdf ? 'border-[#1B4D89] bg-blue-50' : 'border-slate-300 hover:border-[#1B4D89] hover:bg-slate-50'
                  }`}
                >
                  <input type="file" accept="application/pdf" onChange={handlePdfChange} className="hidden" id="pdf-upload" />
                  <label htmlFor="pdf-upload" className="flex flex-col items-center cursor-pointer w-full">
                    <UploadCloud className={`w-6 h-6 mb-2 ${isDraggingPdf ? 'text-[#1B4D89]' : 'text-slate-400'}`} />
                    <span className="text-xs text-slate-600">Upload PDF Proposal</span>
                    <span className="text-[10px] text-slate-400 mt-1">Full technical documents, BoQ, etc.</span>
                  </label>
                </div>
              ) : (
                <div className="h-32 flex gap-3 p-3 bg-blue-50 border border-blue-100 rounded-lg relative">
                  {/* Thumbnail Preview */}
                  <div className="w-24 h-full bg-white border border-slate-200 rounded shadow-sm overflow-hidden flex-shrink-0 relative group cursor-pointer" onClick={() => setShowPdfModal(true)}>
                    {pdfThumbnail ? (
                      <img src={pdfThumbnail} alt="PDF Preview" className="w-full h-full object-cover object-top" />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center bg-slate-100">
                        <FileText className="w-8 h-8 text-slate-300" />
                      </div>
                    )}
                    <div className="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition-colors flex items-center justify-center">
                       <Maximize2 className="w-6 h-6 text-white opacity-0 group-hover:opacity-100 transition-opacity drop-shadow-md" />
                    </div>
                  </div>

                  <div className="flex flex-col justify-between flex-1 py-1">
                    <div>
                      <h4 className="text-sm font-bold text-[#1B4D89] truncate max-w-[180px]">{pdfFile.name}</h4>
                      <div className="flex items-center gap-2 text-xs text-slate-500 mt-1">
                        <span>{(pdfFile.size / 1024 / 1024).toFixed(2)} MB</span>
                        {pdfPageCount > 0 && (
                          <>
                            <span>•</span>
                            <span className="flex items-center gap-1"><File className="w-3 h-3" /> {pdfPageCount} Pages</span>
                          </>
                        )}
                      </div>
                    </div>
                    
                    <button 
                      type="button"
                      onClick={() => setShowPdfModal(true)}
                      className="flex items-center gap-1.5 text-xs font-medium text-[#1B4D89] hover:text-[#163E75] mt-auto"
                    >
                      <Eye className="w-3 h-3" /> Preview Full PDF
                    </button>
                  </div>

                  <button 
                    type="button"
                    onClick={removePdf}
                    className="absolute top-2 right-2 p-1 bg-white/50 hover:bg-white rounded-full text-slate-500 hover:text-red-500 transition-colors"
                    title="Remove file"
                  >
                    <X className="w-4 h-4" />
                  </button>
                </div>
              )}
            </div>

            {/* Visual Assets Section */}
            <div>
               <h4 className="text-sm font-bold text-slate-800 font-['Barlow'] mb-4 flex items-center gap-2">
                 <ImageIcon className="w-4 h-4 text-[#1B4D89]" />
                 Visual Assets
               </h4>
               {/* 2x2 Grid for Consistent Sizing */}
               <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                 <ImageUploader 
                    label="Key Visual / Cover Image" 
                    file={coverImageFile} 
                    setFile={setCoverImageFile} 
                 />
                 <ImageUploader 
                    label="Facade Concept" 
                    file={facadeImageFile} 
                    setFile={setFacadeImageFile}
                    icon={Home}
                 />
                 <MultiImageUploader 
                     label="Interior Concept(s)"
                     files={interiorFiles}
                     setFiles={setInteriorFiles}
                     icon={Armchair}
                 />
                 <MultiImageUploader 
                     label="2D Layout(s)"
                     files={layoutFiles}
                     setFiles={setLayoutFiles}
                     icon={Grid}
                 />
               </div>
            </div>
          </div>

          <div className="flex gap-3 pt-4">
            <button type="button" onClick={onCancel} disabled={isSubmitting} className="px-4 py-2 text-slate-600 hover:text-slate-800 disabled:opacity-50">Cancel</button>
            <button type="submit" disabled={isSubmitting} className="flex-1 bg-[#1B4D89] hover:bg-[#163E75] text-white py-2 rounded-lg flex items-center justify-center gap-2 transition-colors disabled:opacity-50">
              {isSubmitting ? (
                <>
                  <span className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></span>
                  Processing Files...
                </>
              ) : (
                <>
                  <Send className="w-4 h-4" /> Submit Proposal
                </>
              )}
            </button>
          </div>
        </form>
      </div>

      {/* PDF Preview Modal */}
      {showPdfModal && pdfPreviewUrl && (
        <div className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm" role="dialog" aria-modal="true">
          <div className="bg-white rounded-xl shadow-2xl w-full max-w-4xl h-[85vh] flex flex-col animate-fade-in">
            <div className="flex justify-between items-center p-4 border-b border-slate-200">
              <h3 className="font-bold text-slate-800 font-['Barlow'] flex items-center gap-2">
                <FileText className="w-5 h-5 text-[#FD5E53]" />
                Proposal Preview
              </h3>
              <button 
                onClick={() => setShowPdfModal(false)}
                className="p-2 hover:bg-slate-100 rounded-full text-slate-500 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="flex-1 bg-slate-100 p-1 overflow-hidden relative">
              <iframe 
                src={pdfPreviewUrl} 
                className="w-full h-full rounded border border-slate-200"
                title="PDF Preview"
                type="application/pdf"
              />
            </div>
            <div className="p-4 border-t border-slate-200 flex justify-end">
               <button 
                onClick={() => setShowPdfModal(false)}
                className="px-4 py-2 bg-[#1B4D89] text-white rounded-lg hover:bg-[#163E75] transition-colors"
               >
                 Close Preview
               </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default ProposalForm;