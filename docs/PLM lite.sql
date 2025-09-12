CREATE TABLE `PartUsage` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `child_id` VARCHAR(255) NOT NULL,
  `parent_id` VARCHAR(255) NOT NULL,
  `quantity` INT NOT NULL
);

CREATE TABLE `Part` (
  `bigintid` VARCHAR(255) PRIMARY KEY NOT NULL,
  `titlechar` VARCHAR(255) NOT NULL,
  `stage` VARCHAR(255) NOT NULL,
  `level` VARCHAR(255) NOT NULL,
  `creator` VARCHAR(255) NOT NULL,
  `create_time` TIMESTAMP NOT NULL
);

CREATE TABLE `ChangeDocument` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `changetask_id` VARCHAR(255) NOT NULL,
  `updated_itemid` VARCHAR(255) NOT NULL,
  `previous_itemid` VARCHAR(255) NOT NULL
);

CREATE TABLE `Change` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `stage` VARCHAR(255) NOT NULL,
  `class` VARCHAR(255) NOT NULL,
  `prduct` VARCHAR(255) NOT NULL,
  `status` VARCHAR(255) NOT NULL,
  `varcharcreator` VARCHAR(255) NOT NULL,
  `create_time` TIMESTAMP NOT NULL,
  `change_reason` VARCHAR(255) NOT NULL,
  `document_beforechange` VARCHAR(255) NOT NULL,
  `document_afterchange` VARCHAR(255) NOT NULL
);

CREATE TABLE `Task` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `process` VARCHAR(255) NOT NULL,
  `task_type` VARCHAR(255) NOT NULL,
  `status` VARCHAR(255) NOT NULL,
  `assigned_to` VARCHAR(255) NOT NULL,
  `created_time` TIMESTAMP NOT NULL
);

CREATE TABLE `DocumentMaster` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `creator` VARCHAR(255) NOT NULL,
  `create_time` TIMESTAMP NOT NULL,
  `category` VARCHAR(255) NOT NULL
);

CREATE TABLE `Document` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `creator` VARCHAR(255) NOT NULL,
  `create_time` TIMESTAMP NOT NULL,
  `category` VARCHAR(255) NOT NULL,
  `masterid` VARCHAR(255) NOT NULL,
  `version` VARCHAR(255) NOT NULL,
  `revision` VARCHAR(255) NOT NULL,
  `stage` VARCHAR(255) NOT NULL,
  `status` VARCHAR(255) NOT NULL
);

CREATE TABLE `DocumentPartLink` (
  `link_id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `part_id` VARCHAR(255) NOT NULL,
  `document_id` VARCHAR(255) NOT NULL
);

CREATE TABLE `ChangePart` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `changetask_id` VARCHAR(255) NOT NULL,
  `part_id` VARCHAR(255) NOT NULL
);

CREATE TABLE `TaskSignoff` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `task_id` VARCHAR(255) NOT NULL,
  `user_or_group` VARCHAR(255) NOT NULL,
  `decision` VARCHAR(255) NOT NULL,
  `decision_time` TIMESTAMP NOT NULL,
  `comment` TEXT NOT NULL
);

CREATE TABLE `User` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `fullname` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `lastsync` TIMESTAMP NOT NULL
);

ALTER TABLE `TaskSignoff` ADD CONSTRAINT `tasksignoff_task_id_foreign` FOREIGN KEY (`task_id`) REFERENCES `Task` (`id`);

ALTER TABLE `ChangeDocument` ADD CONSTRAINT `changedocument_changetask_id_foreign` FOREIGN KEY (`changetask_id`) REFERENCES `Change` (`id`);

ALTER TABLE `ChangeDocument` ADD CONSTRAINT `changedocument_previous_itemid_foreign` FOREIGN KEY (`previous_itemid`) REFERENCES `Document` (`id`);

ALTER TABLE `ChangePart` ADD CONSTRAINT `changepart_part_id_foreign` FOREIGN KEY (`part_id`) REFERENCES `Part` (`bigintid`);

ALTER TABLE `Document` ADD CONSTRAINT `document_masterid_foreign` FOREIGN KEY (`masterid`) REFERENCES `DocumentMaster` (`id`);

ALTER TABLE `DocumentPartLink` ADD CONSTRAINT `documentpartlink_document_id_foreign` FOREIGN KEY (`document_id`) REFERENCES `Document` (`id`);

ALTER TABLE `PartUsage` ADD CONSTRAINT `partusage_parent_id_foreign` FOREIGN KEY (`parent_id`) REFERENCES `Part` (`bigintid`);

ALTER TABLE `ChangePart` ADD CONSTRAINT `changepart_changetask_id_foreign` FOREIGN KEY (`changetask_id`) REFERENCES `Change` (`id`);

ALTER TABLE `DocumentPartLink` ADD CONSTRAINT `documentpartlink_part_id_foreign` FOREIGN KEY (`part_id`) REFERENCES `Part` (`bigintid`);

ALTER TABLE `ChangeDocument` ADD CONSTRAINT `changedocument_updated_itemid_foreign` FOREIGN KEY (`updated_itemid`) REFERENCES `Document` (`id`);

ALTER TABLE `PartUsage` ADD CONSTRAINT `partusage_child_id_foreign` FOREIGN KEY (`child_id`) REFERENCES `Part` (`bigintid`);
