import React, { useEffect, useState } from "react";

type PageSummary = {
  id: number;
  title: string;
  spaceKey: string;
  parentPageId?: string | null;
};

async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`http://localhost:9091${path}`, {
    headers: { "Content-Type": "application/json" },
    ...init,
  });
  if (!res.ok) {
    throw new Error(await res.text());
  }
  return res.json();
}

export default function PagesList() {
  const [pages, setPages] = useState<PageSummary[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    api<PageSummary[]>("/api/pages")
      .then(setPages)
      .catch((e) => setError(String(e)));
  }, []);

  const openDetails = (id: number) => {
    const u = new URL(window.location.href);
    u.searchParams.set("id", String(id));
    u.searchParams.delete("create");
    window.history.pushState({}, "", u);
    window.dispatchEvent(new PopStateEvent("popstate"));
  };

  const openCreate = () => {
    const u = new URL(window.location.href);
    u.searchParams.set("create", "1");
    u.searchParams.delete("id");
    window.history.pushState({}, "", u);
    window.dispatchEvent(new PopStateEvent("popstate"));
  };

  const deletePage = async (id: number) => {
    if (!confirm("Удалить страницу #" + id + "?")) return;
    await fetch(`http://localhost:9091/api/pages/${id}`, { method: "DELETE" });
    setPages((p) => p.filter((i) => i.id !== id));
  };

  return (
    <div style={{ maxWidth: 920, margin: "0 auto", padding: 20 }}>
      {/* Верхняя панель */}
      <div style={{ display: "flex", gap: 10, marginBottom: 16 }}>
        <button style={{ padding: "6px 12px" }} onClick={() => { window.location.href = window.location.pathname; }}>
          Страницы
        </button>
        <button style={{ padding: "6px 12px" }} onClick={openCreate}>
          Создать страницу
        </button>
      </div>

      <h2 style={{ marginBottom: 12 }}>Страницы</h2>
      {error && <div style={{ color: "red", marginBottom: 10 }}>{error}</div>}
      {pages.length === 0 && <div style={{ color: "#666" }}>Пока пусто. Нажми «Создать страницу».</div>}

      <div style={{ display: "grid", gap: 10 }}>
        {pages.map((p) => (
          <div
            key={p.id}
            style={{
              border: "1px solid #ddd",
              borderRadius: 8,
              padding: 12,
              background: "#fafafa",
            }}
          >
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 12 }}>
              <div style={{ minWidth: 0 }}>
                <div
                  title="Открыть детали"
                  onClick={() => openDetails(p.id)}
                  style={{ fontWeight: 700, cursor: "pointer", overflow: "hidden", textOverflow: "ellipsis" }}
                >
                  #{p.id} — {p.title || "(без заголовка)"}
                </div>
                <div style={{ color: "#666", fontSize: 12, marginTop: 2 }}>
                  space: {p.spaceKey || "—"} · parent: {p.parentPageId || "—"}
                </div>
              </div>

              <div style={{ display: "flex", gap: 8, flexShrink: 0 }}>
                <button onClick={() => openDetails(p.id)}>Редактировать</button>
                <button onClick={() => deletePage(p.id)}>Удалить</button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}