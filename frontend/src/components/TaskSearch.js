import React, { useState } from 'react';
import axios from 'axios';
import { TextField, Button, List, ListItem, Typography, CircularProgress } from '@mui/material';

const TaskSearch = () => {
  const [keyword, setKeyword] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);

  const handleSearch = async () => {
    if (!keyword.trim()) return;

    setLoading(true);
    try {
      const response = await axios.get(`http://localhost:8082/search?keyword=${keyword}`);
      setResults(response.data);
    } catch (err) {
      console.error("Error searching tasks:", err);
    }
    setLoading(false);
  };

  return (
    <div style={{ marginTop: 20 }}>
      <TextField
        label="Search tasks"
        variant="outlined"
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        style={{ marginRight: 10 }}
      />
      <Button variant="contained" onClick={handleSearch}>Search</Button>

      {loading && <CircularProgress size={24} style={{ marginLeft: 15 }} />}

      <List>
        {results.length > 0 && (
          <Typography variant="h6" style={{ marginTop: 20 }}>
            Search Results:
          </Typography>
        )}
        {results.map((task) => (
          <ListItem key={task.id}>
            <strong>{task.title}</strong>: {task.description}
          </ListItem>
        ))}
      </List>
    </div>
  );
};

export default TaskSearch;
