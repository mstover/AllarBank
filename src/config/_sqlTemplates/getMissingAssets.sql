select primary_label,paths.path from $family inner join ${family}_paths paths on ${family}.path_id=paths.path_id where
paths.path='$sql.escapeSql($pathName)' and
primary_label not in (
#foreach($da in $existing)
 #if($da.path == $pathName)'$!sql.escapeSql($da.primary)', #end
#end '')

union

select primary_label,paths.path from $family inner join ${family}_paths paths on ${family}.path_id=paths.path_id where
#if($searchBlankPaths)paths.path = '' or#end (paths.path like '$sql.escapeSql($pathName)%' and
paths.path not in (
#foreach($path in $paths) #if($velocityCount == 1)'$sql.escapeSql($pathName)',#end
  #if($path.startsWith($pathName))'$!sql.escapeSql($path)', #end
#end ''))