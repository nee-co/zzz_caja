# create_directories

# --- !Ups
CREATE TABLE `directories` (
  `id` INTEGER NOT NULL AUTO_INCREMENT COMMENT 'AUTO_INCREMENT',
  `parent_id` INTEGER COMMENT '親ディレクトリ',
  `user_ids` VARCHAR(255) COMMENT '対象ユーザ',
  `college_codes` VARCHAR(255) COMMENT '対象カレッジ',
  `name` VARCHAR(50) NOT NULL COMMENT 'ディレクトリ名',
  `inserted_by` INTEGER NOT NULL COMMENT '作成者',
  `inserted_at` DATETIME NOT NULL COMMENT '作成日時',
  `updated_at` DATETIME NOT NULL COMMENT '更新日',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO directories VALUE (0, NULL, NULL, "a,b,c,d,e,f,g", "top/", 0, now(), now());

# --- !Downs
DROP TABLE directories;