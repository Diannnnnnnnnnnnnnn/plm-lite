import React, { useState, useEffect } from "react";
import axios from "axios";

function TaskList() {
  const [tasks, setTasks] = useState([]);

  useEffect(() => {
    axios.get("http://localhost:8083/tasks")
      .then(response => setTasks(response.data))
      .catch(error => console.error("Error fetching tasks:", error));
  }, []);

  return (
    <div>
      <h2>Tasks</h2>
      <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
        <ul>
          {tasks.map(task => (
            <li key={task.id}>
              {task.name} - {task.description}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

export default TaskList;
