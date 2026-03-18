import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE ?? "http://localhost:9091",
});

// ----- Pages -----

export const getPages = () =>
  api.get("/api/pages").then(r => r.data);

export const getPage = (id: number) =>
  api.get(`/api/pages/${id}`).then(r => r.data);

export const createPage = (body: any) =>
  api.post("/api/pages", body).then(r => r.data);

export const updatePage = (id: number, body: any) =>
  api.put(`/api/pages/${id}`, body).then(r => r.data);

export const deletePage = (id: number) =>
  api.delete(`/api/pages/${id}`);

// ----- Attachments -----

export const uploadAttachment = async (file: File, description?: string) => {
  const fd = new FormData();
  fd.append("file", file);
  if (description) fd.append("description", description);

  const res = await api.post(`/api/attachments`, fd, {
    headers: { "Content-Type": "multipart/form-data" },
  });

  return res.data;
};

export const addAttachments = (pageId: number, attachmentIds: number[]) =>
  api.post(`/api/pages/${pageId}/attachments`, { attachmentIds })
     .then(r => r.data);

export const removeAttachment = (pageId: number, attachmentId: number) =>
  api.delete(`/api/pages/${pageId}/attachments/${attachmentId}`)
     .then(r => r.data);

export const downloadAttachment = (id: number) =>
  window.open(`${api.defaults.baseURL}/api/attachments/${id}/download`, "_blank");