import React, { useState, useEffect } from "react";
import taskService from "../services/taskService";

function TaskList() {
  const [tasks, setTasks] = useState([]);

  useEffect(() => {
    const fetchTasks = async () => {
      try {
        const data = await taskService.getAllTasks();
        setTasks(data);
      } catch (error) {
        console.error("Error fetching tasks:", error);
      }
    };
    
    fetchTasks();
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
