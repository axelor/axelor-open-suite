const LinkIcon = () => {
  return (
    <div style={{ position: "absolute", bottom: "4px", right: "4px" }}>
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="12"
        height="12"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1"
        stroke-linecap="round"
        stroke-linejoin="round"
        style={{ color: "#555" }}
      >
        <path d="M21 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h6" />
        <path d="m21 3-9 9" />
        <path d="M15 3h6v6" />
      </svg>
    </div>
  );
};

export default LinkIcon;
