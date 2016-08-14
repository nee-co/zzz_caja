# create_directories

# --- !Ups
CREATE TABLE `directories` (
  `id` INTEGER NOT NULL AUTO_INCREMENT COMMENT 'AUTO_INCREMENT',
  `parent_id` INTEGER COMMENT '親ディレクトリ',
  `user_ids` varchar(255) DEFAULT NULL COMMENT '対象ユーザ',
  `college_ids` varchar(255) DEFAULT NULL COMMENT '対象カレッジ',
  `name` varchar(50) NOT NULL COMMENT 'ディレクトリ名',
  `inserted_by` INTEGER NOT NULL COMMENT '作成者',
  `inserted_at` datetime NOT NULL COMMENT '作成日時',
  `updated_at` datetime NOT NULL COMMENT '更新日',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

# --- !Downs
DROP TABLE directories;