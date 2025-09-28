import React, { useState } from 'react';
import {
  Box,
  Typography,
  IconButton,
  Collapse,
  List,
  ListItem,
  ListItemButton,
  Chip,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  ChevronRight as ChevronRightIcon,
  AccountTree as TreeIcon,
} from '@mui/icons-material';

const TreeItem = ({ node, level = 0, onNodeSelect, selectedNode }) => {
  const [expanded, setExpanded] = useState(false);
  const hasChildren = node.children && node.children.length > 0;

  const handleToggle = () => {
    if (hasChildren) {
      setExpanded(!expanded);
    }
  };

  const handleSelect = () => {
    onNodeSelect(node);
  };

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'active': return 'success';
      case 'draft': return 'warning';
      case 'obsolete': return 'error';
      default: return 'default';
    }
  };

  return (
    <>
      <ListItem
        disablePadding
        sx={{
          pl: level * 2,
          backgroundColor: selectedNode?.id === node.id ? 'action.selected' : 'transparent'
        }}
      >
        <ListItemButton
          onClick={handleSelect}
          sx={{
            py: 0.5,
            '&:hover': {
              backgroundColor: 'action.hover'
            }
          }}
        >
          <Box display="flex" alignItems="center" width="100%">
            <IconButton
              size="small"
              onClick={(e) => {
                e.stopPropagation();
                handleToggle();
              }}
              sx={{
                mr: 0.5,
                visibility: hasChildren ? 'visible' : 'hidden',
                width: 24,
                height: 24
              }}
            >
              {expanded ? <ExpandMoreIcon fontSize="small" /> : <ChevronRightIcon fontSize="small" />}
            </IconButton>

            <TreeIcon sx={{ mr: 1, fontSize: 16, color: 'action.active' }} />

            <Box flex={1}>
              <Typography
                variant="body2"
                fontWeight={level === 0 ? 600 : 400}
                noWrap
              >
                {node.description || node.title || node.bomNumber}
              </Typography>
              {node.status && (
                <Chip
                  label={node.status}
                  size="small"
                  color={getStatusColor(node.status)}
                  sx={{
                    mt: 0.5,
                    height: 18,
                    fontSize: '0.65rem',
                    '& .MuiChip-label': {
                      px: 0.5
                    }
                  }}
                />
              )}
            </Box>
          </Box>
        </ListItemButton>
      </ListItem>

      {hasChildren && (
        <Collapse in={expanded} timeout="auto" unmountOnExit>
          <List component="div" disablePadding>
            {node.children.map((child) => (
              <TreeItem
                key={child.id}
                node={child}
                level={level + 1}
                onNodeSelect={onNodeSelect}
                selectedNode={selectedNode}
              />
            ))}
          </List>
        </Collapse>
      )}
    </>
  );
};

export default function TreeView({ data, onNodeSelect, selectedNode }) {
  return (
    <Box sx={{
      height: '100%',
      border: '1px solid',
      borderColor: 'divider',
      borderRadius: 1,
      overflow: 'hidden',
      '&::-webkit-scrollbar': {
        display: 'none'
      },
      scrollbarWidth: 'none'
    }}>
      <Box sx={{
        p: 2,
        borderBottom: '1px solid',
        borderColor: 'divider',
        backgroundColor: 'grey.50'
      }}>
        <Typography variant="h6" component="div">
          BOM Structure
        </Typography>
      </Box>

      <List component="nav" sx={{ py: 0, overflow: 'hidden' }}>
        {data.map((node) => (
          <TreeItem
            key={node.id}
            node={node}
            onNodeSelect={onNodeSelect}
            selectedNode={selectedNode}
          />
        ))}
      </List>
    </Box>
  );
}