package org.embulk.formatter.sprintf;

import java.util.List;

import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.config.ConfigSource;
import org.embulk.config.Task;
import org.embulk.config.TaskSource;
import org.embulk.spi.Column;
import org.embulk.spi.Exec;
import org.embulk.spi.FileOutput;
import org.embulk.spi.FormatterPlugin;
import org.embulk.spi.Page;
import org.embulk.spi.PageOutput;
import org.embulk.spi.PageReader;
import org.embulk.spi.Schema;
import org.embulk.spi.time.TimestampFormatter;
import org.embulk.spi.type.Types;
import org.embulk.spi.util.LineEncoder;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class SprintfFormatterPlugin
        implements FormatterPlugin
{
    private static final Logger logger = Exec.getLogger(SprintfFormatterPlugin.class);

    public interface PluginTask
            extends Task, LineEncoder.EncoderTask, TimestampFormatter.Task
    {
        @Config("column_keys")
        public List<String> getColumnKeys();

        @Config("format")
        public String getFormat();
        
        @Config("null_string")
        @ConfigDefault("\"\"")
        public String getNullString();
    }

    @Override
    public void transaction(ConfigSource config, Schema schema,
            FormatterPlugin.Control control)
    {
        PluginTask task = config.loadConfig(PluginTask.class);

        control.run(task.dump());
    }

    private Schema getOutputSchema(List<String> columnKeys, Schema inputSchema)
    {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
        for (String columnKey : columnKeys) {
            builder.add(inputSchema.lookupColumn(columnKey));
        }
        return new Schema(builder.build());
    }

    @Override
    public PageOutput open(final TaskSource taskSource, final Schema inputSchema,
            final FileOutput output)
    {
        final PluginTask task = taskSource.loadTask(PluginTask.class);
        final LineEncoder encoder = new LineEncoder(output, task);
        final String nullString = task.getNullString();
        final Schema outputSchema = getOutputSchema(task.getColumnKeys(), inputSchema);

        encoder.nextFile();

        return new PageOutput() {
            private final PageReader pageReader = new PageReader(inputSchema);

            public void add(Page page)
            {
                pageReader.setPage(page);
                while (pageReader.nextRecord()) {
                    List<Object> values = Lists.newArrayList();
                    for (Column column : outputSchema.getColumns()) {
                        if (pageReader.isNull(column)) {
                            values.add(nullString);
                            continue;
                        }
                        if (Types.STRING.equals(column.getType())) {
                            values.add(pageReader.getString(column));
                        } else if (Types.BOOLEAN.equals(column.getType())) {
                            values.add(pageReader.getBoolean(column));
                        } else if (Types.DOUBLE.equals(column.getType())) {
                            values.add(pageReader.getDouble(column));
                        } else if (Types.LONG.equals(column.getType())) {
                            values.add(pageReader.getLong(column));
                        } else if (Types.TIMESTAMP.equals(column.getType())) {
                            values.add(pageReader.getTimestamp(column));
                        } else if (Types.JSON.equals(column.getType())) {
                            values.add(pageReader.getJson(column));
                        }
                    }
                    logger.debug("record => {}", values);
                    final String text = String.format(task.getFormat(), (Object[]) values.toArray());
                    encoder.addText(text);
                }
            }

            public void finish()
            {
                encoder.finish();
            }

            public void close()
            {
                encoder.close();
            }
        };
    }
}
