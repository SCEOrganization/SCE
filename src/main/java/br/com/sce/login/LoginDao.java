package br.com.sce.login;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class LoginDao {
	
	
	private JdbcTemplate jdbcTemplate;
	
	private final String PARAMETRIZACAO = "{? = call pSelParametrizacao(?)}";
	
	@Autowired
	public LoginDao(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource); 
	}
	
	public boolean validaUsuario(User user){
		jdbcTemplate.query("select id,nome from User where email = ? and password = ?"
				, new Object[]{ user.getEmail(), user.getPassword()},  new RowMapper<User>() {

					@Override
					public User mapRow(ResultSet rs, int arg)
							throws SQLException {
						user.setId(rs.getLong(1));
						user.setNome(rs.getString(2));
						return user;
					}
				});
		
		return user.getId() != null;		
	}
	
	public User carregarParametrizacao(User user){
		return jdbcTemplate.execute(new CallableStatementCreator() {
			
			private CallableStatement prepareCall;

			@Override
			public CallableStatement createCallableStatement(Connection conn)
					throws SQLException {
				prepareCall = conn.prepareCall(PARAMETRIZACAO);
				prepareCall.setLong("id", user.getId());
				return prepareCall;
			}
		}, new CallableStatementCallback<User>() {

			@Override
			public User doInCallableStatement(CallableStatement stmt)
					throws SQLException, DataAccessException {
				int idParametro = stmt.getInt("idParametrizacao");
				Permissao permissao = Permissao.values()[idParametro];
				user.setPermissao(permissao);
				return user;
			}
		});

	}
	
	public void gravarUsuario(User user){
		String sql = "insert into User(nome,email,password) values(?,?,?)";
		Object[] objects = new Object[]{user.getNome(), user.getEmail(), user.getPassword()};
		int[] types = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
		jdbcTemplate.update(sql, objects, types);
	}
	
}
