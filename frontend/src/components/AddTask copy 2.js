import React, { useState } from "react";
import axios from "axios";
import { TextField, Button, Container, Typography, CircularProgress } from "@mui/material";

function AddTask() {
  // State for the form fields
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [userId, setUserId] = useState("");
  const [loading, setLoading] = useState(false);  // Loading state for the button

  const handleSubmit = (e) => {
    e.preventDefault();
    setLoading(true);  // Start loading when the request is sent
    axios
      .post("http://localhost:8083/tasks", {
        name,
        description,
        userId,
      })
      .then(() => {
        alert("Task added successfully!");
        setName("");
        setDescription("");
        setUserId("");
      })
      .catch((error) => console.error("Error adding task:", error))
      .finally(() => setLoading(false));  // Stop loading when the request is complete
  };

  return (
    <Container maxWidth="sm" sx={{ paddingTop: 5 }}>
      <Typography variant="h4" gutterBottom>
        Add New Task
      </Typography>
      <form onSubmit={handleSubmit}>
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
