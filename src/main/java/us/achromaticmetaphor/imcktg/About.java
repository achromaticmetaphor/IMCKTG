package us.achromaticmetaphor.imcktg;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_listview)
public class About extends AppCompatActivity {

  private static final String[] cmds = {"About app", "About icons"};
  private static final int[] texts = {R.string.about_code, R.string.about_icons};

  @ViewById ListView cmdlist;

  @AfterViews
  protected void load() {
    cmdlist.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cmds));
    cmdlist.setOnItemClickListener((AdapterView<?> av, View v, int pos, long id) -> AboutText_.intent(About.this).about_text(texts[pos]).start());
  }
}
