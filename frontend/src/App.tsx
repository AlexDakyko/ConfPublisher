import React, { useEffect, useState } from "react";
import PagesList from "./pages/PagesList";
import PageDetails from "./pages/PageDetails";
import PageForm from "./pages/PageForm";

export default function App() {
  const [, forceUpdate] = useState(0);

  // слушаем переходы по истории (из наших кнопок)
  useEffect(() => {
    const handler = () => forceUpdate((x) => x + 1);
    window.addEventListener("popstate", handler);
    return () => window.removeEventListener("popstate", handler);
  }, []);

  const url = new URL(window.location.href);

  const id = url.searchParams.get("id");
  const isCreate = url.searchParams.get("create");

  if (id) return <PageDetails />;
  if (isCreate) return <PageForm />;

  return <PagesList />;
}