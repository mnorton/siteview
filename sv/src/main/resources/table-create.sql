create table site_registry (
	id bigint autoincrement primary key,
	name varchar(100) not null,
	path varchar(1000) not null
);

create table page_registry (
	id varchar(40) not null,
	site varchar(100) not null,
	name varchar(100) not null,
	file varchar(100) not null,
	path varchar(1000) not null,
	archived char(1) not null default 'F';
);

/* alter table page_registry add column site varchar(100) not null after id;  */

insert into site_registry
	(name, path)
	values
	('nolaria', '/webapps/nolaria');
	
insert into page_registry
	(id, site, name, file, path, archived)
	values
	('961d30bb-c47b-4908-9762-d5918d477319', 'nolaria', 'Home', 'home.html', 'T');

