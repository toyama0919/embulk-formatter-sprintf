Embulk::JavaPlugin.register_formatter(
  "sprintf", "org.embulk.formatter.sprintf.SprintfFormatterPlugin",
  File.expand_path('../../../../classpath', __FILE__))
