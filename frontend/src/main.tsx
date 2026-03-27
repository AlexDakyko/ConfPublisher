import React from "react";
import ReactDOM from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import App from "./App";
import PagesList from "./pages/PagesList";
import PageDetails from "./pages/PageDetails";
import PageForm from "./pages/PageForm";
import "./index.css";

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: [
      { index: true, element: <PagesList /> },
      { path: "pages/new", element: <PageForm mode="create" /> },
      { path: "pages/:id", element: <PageDetails /> },
      { path: "pages/:id/edit", element: <PageForm mode="edit" /> },
    ],
  },
]);

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
);