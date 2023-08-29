package us.achromaticmetaphor.imcktg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class About extends Activity {
  private static final String[] cmds =
          new String[] {"About app", "About icons"};
  private static final int[] texts = {
          R.string.about_code, R.string.about_icons};

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_listview);
    final ListView cmdlist = findViewById(R.id.cmdlist);
    cmdlist.setAdapter(
            new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cmds));
    cmdlist.setOnItemClickListener(
            (AdapterView<?> av, View v, int pos, long id) -> {
              Intent intent = new Intent(this, AboutText.class);
              intent.putExtra("about_text", texts[pos]);
              startActivity(intent);
            });
  }
}
