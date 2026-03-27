import React, { useState } from "react";

type CreatedPage = { id: number };

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

export default function PageForm() {
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [spaceKey, setSpaceKey] = useState("TEST");
  const [parentPageId, setParentPageId] = useState("");

  const backToList = () => {
    const u = new URL(window.location.href);
    u.searchParams.delete("create");
    u.searchParams.delete("id");
    window.history.pushState({}, "", u.toString());
    window.dispatchEvent(new PopStateEvent("popstate"));
  };

  const create = async () => {
    if (!title.trim()) {
      alert("Заполни «Заголовок»");
      return;
    }
    const body = {
      title,
      content,
      spaceKey,
      parentPageId: parentPageId || null,
      attachmentIds: [] as number[],
    };
    const created = await api<CreatedPage>("/api/pages", {
      method: "POST",
      body: JSON.stringify(body),
    });

    const u = new URL(window.location.href);
    u.searchParams.delete("create");
    u.searchParams.set("id", String(created.id));
    window.history.pushState({}, "", u.toString());
    window.dispatchEvent(new PopStateEvent("popstate"));
  };

  return (
    <div style={{ maxWidth: 800, margin: "0 auto", padding: 20 }}>
      {/* Верхняя панель */}
      <div style={{ display: "flex", gap: 10, marginBottom: 16 }}>
        <button style={{ padding: "6px 12px" }} onClick={backToList}>Страницы</button>
        <button style={{ padding: "6px 12px", opacity: 0.7, cursor: "default" }} disabled>Создать страницу</button>
      </div>

      <h2 style={{ marginBottom: 12 }}>Создать страницу</h2>

      <div style={{ display: "flex", flexDirection: "column", gap: 10, maxWidth: 720 }}>
        <label>
          Заголовок:
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            style={{ width: "100%", marginTop: 4 }}
            placeholder="Например: Моя первая страница"
          />
        </label>

        <label>
          Контент:
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            rows={8}
            style={{ width: "100%", marginTop: 4 }}
            placeholder="Основной текст страницы"
          />
        </label>

        <label>
          Space Key:
          <input
            value={spaceKey}
            onChange={(e) => setSpaceKey(e.target.value)}
            style={{ marginTop: 4 }}
          />
        </label>

        <label>
          Parent Page ID (опц.):
          <input
            value={parentPageId}
            onChange={(e) => setParentPageId(e.target.value)}
            style={{ marginTop: 4 }}
            placeholder="ID родительской страницы (если нужен)"
          />
        </label>
      </div>

      <div style={{ marginTop: 14, display: "flex", gap: 10 }}>
        <button onClick={create}>Создать</button>
        <button onClick={backToList}>Отмена</button>
      </div>
    </div>
  );
}