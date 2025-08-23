import React, { useState } from "react";
import axios from "axios";

function AddTask() {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [userId, setUserId] = useState("");
  const [files, setFiles] = useState([]);

  const handleFileChange = (e) => {
    setFiles(e.target.files);
  };

  const resetForm = () => {
    setName("");
    setDescription("");
    setUserId("");
    setFiles([]);
  };

  const createTask = async () => {
    const formData = new FormData();
    formData.append("name", name);
    formData.append("description", description);
    formData.append("userId", userId);
    for (let i = 0; i < files.length; i++) {
      formData.append("files", files[i]);
    }

    const response = await axios.post("http://localhost:8083/tasks/create", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    return response.data.id; // Return the task ID
  };

  const handleCreateOnly = async (e) => {
    e.preventDefault();
    try {
      await createTask();
      alert("Task created successfully!");
      resetForm();
    } catch (error) {
      console.error("Error creating task:", error);
      alert("Failed to create task.");
    }
  };

  const handleCreateAndStartProcess = async (e) => {
    e.preventDefault();
    try {
      const taskId = await createTask();
      await axios.post(`http://localhost:8083/tasks/start-process`, null, {
        params: { taskId },
      });
      alert("Task created and process started!");
      resetForm();
    } catch (error) {
      console.error("Error:", error);
      alert("Failed to create task or start process.");
    }
  };

  return (
    <form>
      <h2>Add Task</h2>
      <input
        type="text"
        placeholder="Task Name"
        value={name}
        onChange={(e) => setName(e.target.value)}
        required
      />
      <input
        type="text"
        placeholder="Description"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        required
      />
      <input
        type="text"
        placeholder="User ID"
        value={userId}
        onChange={(e) => setUserId(e.target.value)}
        required
      />
      <input type="file" multiple onChange={handleFileChange} />

      <div style={{ marginTop: "1rem" }}>
        <button onClick={handleCreateOnly}>Create Task Only</button>
        <button onClick={handleCreateAndStartProcess} style={{ marginLeft: "1rem" }}>
          Create Task & Start Process
        </button>
      </div>
    </form>
  );
}

export default AddTask;
