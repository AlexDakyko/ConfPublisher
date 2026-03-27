import React, { useEffect, useMemo, useRef, useState } from "react";

/** ===== Типы ===== */
type AttachmentSummary = {
  id: number;
  filename: string;
  contentType: string;
  size: number;
  description?: string | null;
};

type PageDetailsT = {
  id: number;
  title: string;
  content: string;
  spaceKey: string;
  parentPageId?: string | null;
  createdAt?: string;
  updatedAt?: string;
  attachments: AttachmentSummary[];
};

type PublishResponse = {
  pageId: number;
  remotePageId?: string | null;
  status: string;
  provider?: string | null;
  message?: string | null;
  createdAt?: string;
};

/** ===== API helper ===== */
async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`http://localhost:9091${path}`, {
    headers: { "Content-Type": "application/json" },
    ...init,
  });
  if (!res.ok) {
    const msg = await res.text();
    throw new Error(`${res.status} ${res.statusText}: ${msg}`);
  }
  return res.json();
}

/** ===== Компонент ===== */
export default function PageDetails() {
  const url = new URL(window.location.href);
  const pageId = Number(url.searchParams.get("id"));

  const [details, setDetails] = useState<PageDetailsT | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [editMode, setEditMode] = useState<boolean>(false);
  const [title, setTitle] = useState<string>("");
  const [content, setContent] = useState<string>("");
  const [spaceKey, setSpaceKey] = useState<string>("TEST");
  const [parentPageId, setParentPageId] = useState<string>("");

  const fileRef = useRef<HTMLInputElement | null>(null);
  const [attachDesc, setAttachDesc] = useState<string>("");

  const [publishing, setPublishing] = useState<boolean>(false);
  const [publishStatus, setPublishStatus] = useState<PublishResponse | null>(null);

  useEffect(() => {
    loadDetails();
  }, [pageId]);

  /** Загрузка деталей */
  const loadDetails = async () => {
    if (!pageId) return;
    setLoading(true);
    try {
      const d = await api<PageDetailsT>(`/api/pages/${pageId}`);
      setDetails(d);
      setTitle(d.title);
      setContent(d.content);
      setSpaceKey(d.spaceKey);
      setParentPageId(d.parentPageId || "");
    } catch (e: any) {
      setError(String(e.message || e));
    } finally {
      setLoading(false);
    }
  };

  /** Назад */
  const backToList = () => {
    const u = new URL(window.location.href);
    u.searchParams.delete("id");
    u.searchParams.delete("create");
    window.history.pushState({}, "", u);
    window.dispatchEvent(new PopStateEvent("popstate"));
  };

  /** Сохранение */
  const save = async () => {
    if (!details) return;
    const body = {
      title,
      content,
      spaceKey,
      parentPageId: parentPageId || null,
    };
    const updated = await api<PageDetailsT>(`/api/pages/${details.id}`, {
      method: "PUT",
      body: JSON.stringify(body),
    });
    setDetails(updated);
    setEditMode(false);
  };

  /** Удаление вложения */
  const removeAttachment = async (attachmentId: number) => {
    if (!details) return;
    if (!confirm("Удалить вложение?")) return;
    const updated = await api<PageDetailsT>(
      `/api/pages/${details.id}/attachments/${attachmentId}`,
      { method: "DELETE" }
    );
    setDetails(updated);
  };

  /** Скачивание */
  const downloadAttachment = (attachmentId: number, filename: string) => {
    const a = document.createElement("a");
    a.href = `http://localhost:9091/api/attachments/${attachmentId}/download`;
    a.download = filename;
    a.click();
  };

  /** Загрузка + привязка */
  const uploadAndBind = async () => {
    if (!details) return;
    const file = fileRef.current?.files?.[0];
    if (!file) {
      alert("Выберите файл!");
      return;
    }

    // 1) upload
    const form = new FormData();
    form.append("file", file);
    form.append("description", attachDesc);

    const res = await fetch("http://localhost:9091/api/attachments", {
      method: "POST",
      body: form,
    });
    if (!res.ok) {
      const t = await res.text();
      alert("Ошибка загрузки вложения: " + t);
      return;
    }
    const created = await res.json();

    // Универсально достаём ID, независимо от формы ответа
    const uploadedId =
      (created && (created.id ?? created.attachmentId)) ??
      (created?.attachment?.id ?? created?.data?.id ?? created?.data?.attachmentId);

    if (!uploadedId || isNaN(Number(uploadedId))) {
      console.warn("Не удалось определить ID вложения из ответа:", created);
      alert("Не удалось определить ID загруженного вложения. Смотри консоль.");
      return;
    }

    // 2) привязка к странице
    const updated = await api<PageDetailsT>(`/api/pages/${details.id}/attachments`, {
      method: "POST",
      body: JSON.stringify({ attachmentIds: [Number(uploadedId)] }),
    });

    setDetails(updated);
    setAttachDesc("");
    if (fileRef.current) fileRef.current.value = "";



    setDetails(updated);
    setAttachDesc("");
    if (fileRef.current) fileRef.current.value = "";
  };

  /** Публикация */
  const publishNow = async () => {
    if (!details) return;
    setPublishing(true);
    try {
      const resp = await api<PublishResponse>(`/api/pages/${details.id}/publish`, {
        method: "POST",
      });
      setPublishStatus(resp);
      alert("Публикация запущена");
    } catch (e: any) {
      alert("Ошибка публикации: " + e.message);
    } finally {
      setPublishing(false);
    }
  };

  const loadPublishStatus = async () => {
    if (!details) return;
    try {
      const resp = await api<PublishResponse>(
        `/api/pages/${details.id}/publish/status`
      );
      setPublishStatus(resp);
    } catch (e: any) {
      alert("Статус недоступен: " + e.message);
    }
  };

  /** Переупорядочивание вложений */
  const moveAttachment = async (idx: number, dir: -1 | 1) => {
    if (!details) return;
    const next = idx + dir;
    if (next < 0 || next >= details.attachments.length) return;

    const reordered = [...details.attachments];
    [reordered[idx], reordered[next]] = [reordered[next], reordered[idx]];

    const updated = await api<PageDetailsT>(
      `/api/pages/${details.id}/attachments/reorder`,
      {
        method: "PATCH",
        body: JSON.stringify({
          attachmentIdsInOrder: reordered.map((a) => a.id),
        }),
      }
    );

    setDetails(updated);
  };

  if (loading) return <div>Загрузка…</div>;
  if (error) return <div style={{ color: "red" }}>{error}</div>;
  if (!details) return <div>Страница не найдена</div>;

  return (
    <div style={{ maxWidth: 800, margin: "0 auto", padding: 20 }}>

      {/* Верхние кнопки */}
      <div style={{ display: "flex", gap: 10, marginBottom: 20 }}>
        <button style={{ padding: "6px 12px" }} onClick={backToList}>Страницы</button>
        <button style={{ padding: "6px 12px" }} onClick={loadDetails}>Обновить</button>
      </div>

      {/* Заголовок */}
      <h2 style={{ marginBottom: 10 }}>
        Страница #{details.id}: {details.title}
      </h2>

      {/* Контент */}
      <div style={{ marginBottom: 20 }}>
        <div style={{ whiteSpace: "pre-wrap", marginBottom: 5 }}>{details.content}</div>
        <div style={{ color: "#666" }}>
          space: {details.spaceKey} · parent: {details.parentPageId || "—"}
        </div>
      </div>

      {/* Кнопки действий */}
      <div style={{
        display: "flex",
        gap: 10,
        flexWrap: "wrap",
        marginBottom: 20
      }}>
        <button style={{ padding: "6px 12px" }} onClick={() => setEditMode(true)}>Редактировать</button>
        <button style={{ padding: "6px 12px" }} onClick={publishNow} disabled={publishing}>
          {publishing ? "Публикую…" : "Опубликовать"}
        </button>
        <button style={{ padding: "6px 12px" }} onClick={loadPublishStatus}>Статус публикации</button>
      </div>

      {/* Статус публикации */}
      {publishStatus && (
        <div style={{
          border: "1px solid #ddd",
          borderRadius: 6,
          padding: 12,
          marginBottom: 20
        }}>
          <div><b>Статус:</b> {publishStatus.status}</div>
          <div><b>Провайдер:</b> {publishStatus.provider}</div>
          <div><b>Remote Page ID:</b> {publishStatus.remotePageId}</div>
          <div><b>Сообщение:</b> {publishStatus.message}</div>
          <div><b>Время:</b> {publishStatus.createdAt}</div>
        </div>
      )}

      {/* Вложения */}
      <h3 style={{ marginTop: 30, marginBottom: 10 }}>Вложения</h3>

      {details.attachments.length === 0 ? (
        <div style={{ marginBottom: 15, color: "#666" }}>Нет вложений</div>
      ) : (
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          {details.attachments.map((a, idx) => (
            <div key={a.id} style={{
              border: "1px solid #ddd",
              padding: 10,
              borderRadius: 6,
              background: "#fafafa"
            }}>
              <div style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center"
              }}>

                <div>
                  <div><b>{a.filename}</b></div>
                  <div style={{ fontSize: 12, color: "#666" }}>
                    {a.contentType} · {(a.size / 1024).toFixed(1)} KB
                    {a.description ? ` · ${a.description}` : ""}
                  </div>
                </div>

                <div style={{ display: "flex", gap: 6 }}>
                  <button onClick={() => moveAttachment(idx, -1)}>↑</button>
                  <button onClick={() => moveAttachment(idx, 1)}>↓</button>
                  <button onClick={() => downloadAttachment(a.id, a.filename)}>Скачать</button>
                  <button onClick={() => removeAttachment(a.id)}>Удалить</button>
                </div>

              </div>
            </div>
          ))}
        </div>
      )}

      {/* Добавить вложение */}
      <h4 style={{ marginTop: 20 }}>Добавить вложение</h4>

      <div style={{
        display: "flex",
        gap: 10,
        alignItems: "center",
        flexWrap: "wrap",
        marginTop: 10
      }}>
        <input type="file" ref={fileRef} />
        <input
          style={{ width: 250 }}
          placeholder="Описание (опционально)"
          value={attachDesc}
          onChange={(e) => setAttachDesc(e.target.value)}
        />
        <button style={{ padding: "6px 12px" }} onClick={uploadAndBind}>
          Загрузить и привязать
        </button>
      </div>

      {/* Режим редактирования */}
      {editMode && (
        <div style={{ marginTop: 30 }}>
          <h3>Редактирование страницы</h3>

          <div style={{ display: "flex", flexDirection: "column", gap: 10, maxWidth: 600 }}>
            <label>
              Заголовок:
              <input style={{ width: "100%" }} value={title} onChange={(e) => setTitle(e.target.value)} />
            </label>

            <label>
              Контент:
              <textarea
                rows={8}
                style={{ width: "100%" }}
                value={content}
                onChange={(e) => setContent(e.target.value)}
              />
            </label>

            <label>
              Space Key:
              <input value={spaceKey} onChange={(e) => setSpaceKey(e.target.value)} />
            </label>

            <label>
              Parent Page ID:
              <input value={parentPageId} onChange={(e) => setParentPageId(e.target.value)} />
            </label>

            <div style={{ display: "flex", gap: 10, marginTop: 10 }}>
              <button onClick={save}>Сохранить</button>
              <button onClick={() => setEditMode(false)}>Отмена</button>
            </div>
          </div>

        </div>
      )}

    </div>
  );
}