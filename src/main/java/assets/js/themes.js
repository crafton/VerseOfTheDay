/**
 * Created by craft on 16/07/2016.
 */

$('[data-toggle="confirmation"]').confirmation({
    href: function(elem){
        return $(elem).attr('href');
    }
});
