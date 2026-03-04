import React, { useEffect, useState } from "react";

type Page = {
  id: number;
  title: string;
  content: string;
  spaceKey: string;
  parentPageId?: string | null;
};

type NewPageForm = {
  title: string;
  content: string;
  spaceKey: string;
  parentPageId: string;
};

const initialForm: NewPageForm = {
  title: "",
  content: "",
  spaceKey: "",
  parentPageId: ""
};

const App: React.FC = () => {
  const [pages, setPages] = useState<Page[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [form, setForm] = useState<NewPageForm>(initialForm);
  const [submitting, setSubmitting] = useState(false);

  const loadPages = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch("/api/pages");
      if (!res.ok) {
        throw new Error(`Failed to fetch pages (${res.status})`);
      }
      const data: Page[] = await res.json();
      setPages(data);
    } catch (e: unknown) {
      const message = e instanceof Error ? e.message : "Unknown error";
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPages();
  }, []);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const params = new URLSearchParams();
      params.append("title", form.title);
      params.append("content", form.content);
      params.append("spaceKey", form.spaceKey);
      if (form.parentPageId.trim()) {
        params.append("parentPageId", form.parentPageId.trim());
      }

      const res = await fetch(`/api/pages?${params.toString()}`, {
        method: "POST"
      });
      if (!res.ok) {
        throw new Error(`Failed to create page (${res.status})`);
      }
      setForm(initialForm);
      await loadPages();
    } catch (e: unknown) {
      const message = e instanceof Error ? e.message : "Unknown error";
      setError(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        background: "#0f172a",
        color: "#e5e7eb",
        fontFamily: "system-ui, -apple-system, BlinkMacSystemFont, sans-serif",
        padding: "2rem"
      }}
    >
      <div
        style={{
          maxWidth: "960px",
          margin: "0 auto"
        }}
      >
        <header style={{ marginBottom: "2rem" }}>
          <h1 style={{ fontSize: "2rem", marginBottom: "0.5rem" }}>
            Confluence Publisher
          </h1>
          <p style={{ color: "#9ca3af" }}>
            Compose and publish pages to your Confluence-like workspace.
          </p>
        </header>

        <main
          style={{
            display: "grid",
            gridTemplateColumns: "1.1fr 1fr",
            gap: "1.5rem",
            alignItems: "flex-start"
          }}
        >
          <section
            style={{
              background: "#020617",
              borderRadius: "0.75rem",
              padding: "1.5rem",
              boxShadow: "0 10px 40px rgba(0,0,0,0.4)",
              border: "1px solid #1f2937"
            }}
          >
            <h2 style={{ fontSize: "1.25rem", marginBottom: "1rem" }}>
              New Page
            </h2>
            <form
              onSubmit={handleSubmit}
              style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}
            >
              <label style={{ fontSize: "0.875rem" }}>
                Title
                <input
                  type="text"
                  name="title"
                  value={form.title}
                  onChange={handleChange}
                  required
                  style={{
                    marginTop: "0.25rem",
                    width: "100%",
                    padding: "0.5rem 0.75rem",
                    borderRadius: "0.5rem",
                    border: "1px solid #374151",
                    background: "#020617",
                    color: "#e5e7eb"
                  }}
                />
              </label>
              <label style={{ fontSize: "0.875rem" }}>
                Space key
                <input
                  type="text"
                  name="spaceKey"
                  value={form.spaceKey}
                  onChange={handleChange}
                  required
                  style={{
                    marginTop: "0.25rem",
                    width: "100%",
                    padding: "0.5rem 0.75rem",
                    borderRadius: "0.5rem",
                    border: "1px solid #374151",
                    background: "#020617",
                    color: "#e5e7eb"
                  }}
                />
              </label>
              <label style={{ fontSize: "0.875rem" }}>
                Parent page ID (optional)
                <input
                  type="text"
                  name="parentPageId"
                  value={form.parentPageId}
                  onChange={handleChange}
                  style={{
                    marginTop: "0.25rem",
                    width: "100%",
                    padding: "0.5rem 0.75rem",
                    borderRadius: "0.5rem",
                    border: "1px solid #374151",
                    background: "#020617",
                    color: "#e5e7eb"
                  }}
                />
              </label>
              <label style={{ fontSize: "0.875rem" }}>
                Content
                <textarea
                  name="content"
                  value={form.content}
                  onChange={handleChange}
                  required
                  rows={6}
                  style={{
                    marginTop: "0.25rem",
                    width: "100%",
                    padding: "0.5rem 0.75rem",
                    borderRadius: "0.5rem",
                    border: "1px solid #374151",
                    background: "#020617",
                    color: "#e5e7eb",
                    resize: "vertical"
                  }}
                />
              </label>
              <button
                type="submit"
                disabled={submitting}
                style={{
                  marginTop: "0.5rem",
                  padding: "0.6rem 1rem",
                  borderRadius: "999px",
                  border: "none",
                  background:
                    "linear-gradient(135deg, #4f46e5, #22c55e, #0ea5e9)",
                  color: "#0b1120",
                  fontWeight: 600,
                  cursor: submitting ? "default" : "pointer",
                  opacity: submitting ? 0.7 : 1
                }}
              >
                {submitting ? "Creating..." : "Create page"}
              </button>
              {error && (
                <p style={{ color: "#f97316", fontSize: "0.875rem" }}>{error}</p>
              )}
            </form>
          </section>

          <section
            style={{
              background: "#020617",
              borderRadius: "0.75rem",
              padding: "1.5rem",
              boxShadow: "0 10px 40px rgba(0,0,0,0.4)",
              border: "1px solid #1f2937",
              maxHeight: "80vh",
              overflowY: "auto"
            }}
          >
            <div
              style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                marginBottom: "1rem"
              }}
            >
              <h2 style={{ fontSize: "1.25rem" }}>Pages</h2>
              <button
                type="button"
                onClick={loadPages}
                disabled={loading}
                style={{
                  padding: "0.3rem 0.75rem",
                  borderRadius: "999px",
                  border: "1px solid #374151",
                  background: "#020617",
                  color: "#e5e7eb",
                  fontSize: "0.8rem",
                  cursor: loading ? "default" : "pointer",
                  opacity: loading ? 0.7 : 1
                }}
              >
                {loading ? "Refreshing..." : "Refresh"}
              </button>
            </div>

            {loading && pages.length === 0 && (
              <p style={{ color: "#9ca3af" }}>Loading pages…</p>
            )}

            {!loading && pages.length === 0 && (
              <p style={{ color: "#9ca3af", fontSize: "0.9rem" }}>
                No pages yet. Create your first page using the form.
              </p>
            )}

            <ul
              style={{
                listStyle: "none",
                padding: 0,
                margin: 0,
                display: "flex",
                flexDirection: "column",
                gap: "0.75rem"
              }}
            >
              {pages.map((page) => (
                <li
                  key={page.id}
                  style={{
                    padding: "0.75rem 1rem",
                    borderRadius: "0.75rem",
                    border: "1px solid #1f2937",
                    background:
                      "radial-gradient(circle at top left, #1e293b, #020617)"
                  }}
                >
                  <div
                    style={{
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "baseline",
                      gap: "0.5rem"
                    }}
                  >
                    <h3
                      style={{
                        margin: 0,
                        fontSize: "1rem",
                        fontWeight: 600,
                        color: "#e5e7eb"
                      }}
                    >
                      {page.title}
                    </h3>
                    <span
                      style={{
                        fontSize: "0.75rem",
                        color: "#9ca3af",
                        padding: "0.1rem 0.5rem",
                        borderRadius: "999px",
                        border: "1px solid #1f2937"
                      }}
                    >
                      {page.spaceKey}
                    </span>
                  </div>
                  {page.parentPageId && (
                    <p
                      style={{
                        margin: "0.2rem 0 0.4rem",
                        fontSize: "0.75rem",
                        color: "#6b7280"
                      }}
                    >
                      Parent: {page.parentPageId}
                    </p>
                  )}
                  <p
                    style={{
                      margin: 0,
                      marginTop: "0.4rem",
                      fontSize: "0.85rem",
                      color: "#d1d5db",
                      whiteSpace: "pre-wrap"
                    }}
                  >
                    {page.content.length > 260
                      ? `${page.content.slice(0, 260)}…`
                      : page.content}
                  </p>
                </li>
              ))}
            </ul>
          </section>
        </main>
      </div>
    </div>
  );
};

export default App;

