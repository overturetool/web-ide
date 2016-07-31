package filters;

import com.google.inject.Inject;
import play.http.HttpFilters;
import play.mvc.EssentialAction;
import play.mvc.EssentialFilter;
import play.filters.cors.CORSFilter;

public class Filters extends EssentialFilter implements HttpFilters {
    // TODO : This implementation is a workaround due to a bug in the framework

    @Inject
    private CORSFilter corsFilter;

    @Override
    public EssentialAction apply(EssentialAction next) {
        return corsFilter.asJava().apply(next);
    }

    @Override
    public EssentialFilter[] filters() {
        EssentialFilter[] result = new EssentialFilter[1];
        result[0] = this;
        return result;
    }
}