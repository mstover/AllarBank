update $da.family 
set is_active=0 
where primary_label='$sql.escapeSql($da.primary)' and path_id=$pathId