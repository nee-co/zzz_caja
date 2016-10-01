# create_files

# --- !Ups
CREATE TABLE `files` (
  `id` INTEGER NOT NULL AUTO_INCREMENT COMMENT 'AUTO_INCREMENT',
  `parent_id` INTEGER COMMENT '親ディレクトリ',
  `user_ids` VARCHAR(255) COMMENT '対象ユーザ',
  `college_codes` VARCHAR(255) COMMENT '対象カレッジ',
  `name` VARCHAR(50) NOT NULL COMMENT 'ファイル名',
  `path` VARCHAR(255) NOT NULL COMMENT 'ファイルパス',
  `inserted_by` INTEGER NOT NULL COMMENT '作成者',
  `inserted_at` DATETIME NOT NULL COMMENT '作成日時',
  `updated_at` DATETIME NOT NULL COMMENT '更新日時',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# --- !Downs
DROP TABLE files;