package ipvc.estg.room

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipvc.estg.room.adapters.NotaAdapter
import ipvc.estg.room.entities.Nota
import ipvc.estg.room.viewModel.NotaViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NotaAdapter.OnItemClickListener {

    private lateinit var notaViewModel: NotaViewModel
    private val newWordActivityRequestCode = 1
    private val UpdateActivityRequestCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // recycler view
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = NotaAdapter(this, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // view model
        notaViewModel = ViewModelProvider(this).get(NotaViewModel::class.java)
        notaViewModel.allNotas.observe(this, Observer {notas ->
            // Update the cached copy of the words in the adapter.
            notas?.let { adapter.setNotas(it) }
        })

        //Fab
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, AddNota::class.java)
            startActivityForResult(intent, newWordActivityRequestCode)
        }

        val itemTouchHelperCallback = object: ItemTouchHelper.SimpleCallback( 0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT ) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                notaViewModel.deleteNota( adapter.getPosicaoNota(viewHolder.adapterPosition) )
            }

        }
        val itemTouchHelper = ItemTouchHelper( itemTouchHelperCallback )
        itemTouchHelper.attachToRecyclerView( recyclerview )
    }

    override fun onItemClicked(nota: Nota ) {
        val intent = Intent( this, UpdateNota::class.java)
        intent.putExtra(UpdateNota.EXTRA_ID, nota.id)
        intent.putExtra(UpdateNota.EXTRA_REPLY_NOTA, nota.nota)
        intent.putExtra(UpdateNota.EXTRA_REPLY_TEXTO, nota.texto)
        startActivityForResult(intent, UpdateActivityRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == newWordActivityRequestCode && resultCode == Activity.RESULT_OK) {
            val pnota = data?.getStringExtra(AddNota.EXTRA_REPLY_NOTA)
            val ptexto = data?.getStringExtra(AddNota.EXTRA_REPLY_TEXTO)

            if (pnota!= null && ptexto != null) {
                val nota = Nota(nota = pnota, texto = ptexto)
                notaViewModel.insert(nota)
            }
        } else {
            Toast.makeText(
                applicationContext,
                R.string.empty_not_saved,
                Toast.LENGTH_LONG).show()
        }
        if (requestCode == UpdateActivityRequestCode && resultCode == RESULT_OK) {
            val id = data?.getIntExtra( UpdateNota.EXTRA_ID, -1 )

            val titulo = data?.getStringExtra( UpdateNota.EXTRA_REPLY_NOTA).toString()
            val texto = data?.getStringExtra( UpdateNota.EXTRA_REPLY_TEXTO).toString()
            val nota = Nota(id,titulo,texto)

            notaViewModel.updateNota(nota)
            Toast.makeText(applicationContext,"Nota Editada",Toast.LENGTH_LONG).show()
        }
        else if(requestCode == UpdateActivityRequestCode) {
            Toast.makeText(applicationContext,R.string.empty_not_saved,Toast.LENGTH_LONG).show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.apagartudo -> {
                notaViewModel.deleteAll()
                Toast.makeText(applicationContext,"Todas as notas Apagadas",Toast.LENGTH_LONG).show()
                true

            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}