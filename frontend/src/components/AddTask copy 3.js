import React, { useState } from "react";
import axios from "axios";
import {
  TextField,
  Button,
  Container,
  Typography,
  CircularProgress,
  Box,
} from "@mui/material";

function AddTask() {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [userId, setUserId] = useState("");
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(false);

  const handleFileChange = (e) => {
    setFiles(e.target.files);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    const formData = new FormData();
    formData.append("name", name);
    formData.append("description", description);
    formData.append("userId", userId);

    for (let i = 0; i < files.length; i++) {
      formData.append("files", files[i]);
    }

    try {
      await axios.post("http://localhost:8083/tasks/create", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      alert("Task created successfully!");
      setName("");
      setDescription("");
      setUserId("");
      setFiles([]);
    } catch (error) {
      console.error("Error adding task:", error);
      alert("Failed to create task.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="sm" sx={{ paddingTop: 5 }}>
      <Typography variant="h4" gutterBottom>
        Add New Task
      </Typography>
      <form onSubmit={handleSubmit} encType="multipart/form-data">
        <TextField
          label="Task Name"
          fullWidth
          variant="outlined"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
          sx={{ marginBottom: 2 }}
        />
        <TextField
          label="Description"
          fullWidth
          variant="outlined"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          required
          sx={{ marginBottom: 2 }}
        />
        <TextField
          label="User ID"
          fullWidth
          variant="outlined"
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
          required
          sx={{ marginBottom: 2 }}
        />
        <Box sx={{ marginBottom: 2 }}>
          <input
            type="file"
            multiple
            onChange={handleFileChange}
            style={{ display: "block", marginTop: "8px" }}
          />
        </Box>
        <Button
          type="submit"
          variant="contained"
          color="primary"
          fullWidth
          disabled={loading}
          sx={{ padding: 1.5 }}
        >
          {loading ? <CircularProgress size={24} color="inherit" /> : "Add Task"}
        </Button>
      </form>
    </Container>
  );
}

export default AddTask;
