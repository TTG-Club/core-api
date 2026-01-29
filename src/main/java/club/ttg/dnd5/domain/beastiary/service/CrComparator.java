package club.ttg.dnd5.domain.beastiary.service;

import java.util.Comparator;

public final class CrComparator implements Comparator<String>
{
    public static final CrComparator INSTANCE = new CrComparator();

    private CrComparator()
    {
    }

    @Override
    public int compare(final String a, final String b)
    {
        int byValue = Double.compare(parseCrValue(a), parseCrValue(b));
        if (byValue != 0)
        {
            return byValue;
        }
        return String.CASE_INSENSITIVE_ORDER.compare(a, b);
    }

    private static double parseCrValue(final String cr)
    {
        if (cr == null)
        {
            return 0.0;
        }

        String normalized = cr.trim();
        if (normalized.isEmpty())
        {
            return 0.0;
        }

        if ("—".equals(normalized))
        {
            return -1.0;
        }

        try
        {
            if (normalized.contains("/"))
            {
                String[] parts = normalized.split("/");
                if (parts.length == 2)
                {
                    double numerator = Double.parseDouble(parts[0].trim());
                    double denominator = Double.parseDouble(parts[1].trim());
                    if (denominator == 0.0)
                    {
                        return Double.POSITIVE_INFINITY;
                    }
                    return numerator / denominator;
                }
            }
            return Double.parseDouble(normalized);
        }
        catch (NumberFormatException ex)
        {
            return Double.POSITIVE_INFINITY;
        }
    }
}