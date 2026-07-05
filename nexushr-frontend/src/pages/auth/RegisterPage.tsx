import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { authApi } from '../../api/authApi';
import { Button } from '../../components/ui/Button';

const schema = z.object({
  username: z.string().min(3, 'Min 3 characters'),
  email: z.string().email('Invalid email'),
  password: z.string()
    .min(8, 'Min 8 characters')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$/, 'Must include uppercase, lowercase, and a digit'),
  role: z.enum(['EMPLOYEE', 'MANAGER', 'ADMIN']),
});
type FormData = z.infer<typeof schema>;

export function RegisterPage() {
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { register, handleSubmit, formState: { errors } } =
    useForm<FormData>({ resolver: zodResolver(schema), defaultValues: { role: 'EMPLOYEE' } });

  const onSubmit = async (data: FormData) => {
    setError('');
    setLoading(true);
    try {
      await authApi.register(data);
      navigate('/login');
    } catch (e: any) {
      setError(e?.response?.data?.message ?? 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen relative flex items-center justify-center font-sans overflow-hidden bg-slate-900">
      {/* Animated Background */}
      <div className="absolute top-[-20%] left-[-10%] w-[70%] h-[70%] rounded-full bg-indigo-600/20 blur-[120px] mix-blend-screen animate-pulse"></div>
      <div className="absolute bottom-[-20%] right-[-10%] w-[60%] h-[60%] rounded-full bg-blue-600/20 blur-[120px] mix-blend-screen animate-pulse" style={{ animationDelay: '2s' }}></div>
      <div className="absolute top-[20%] right-[20%] w-[40%] h-[40%] rounded-full bg-purple-600/20 blur-[100px] mix-blend-screen animate-pulse" style={{ animationDelay: '4s' }}></div>

      <div className="relative z-10 w-full max-w-md p-6 sm:p-10 mx-4 bg-white/10 backdrop-blur-2xl border border-white/20 rounded-[2rem] shadow-2xl">
        <div className="text-center mb-10">
          <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-tr from-teal-500 to-emerald-400 rounded-2xl mb-6 shadow-lg shadow-teal-500/30">
            <span className="text-white text-3xl font-extrabold tracking-tight">N</span>
          </div>
          <h1 className="text-3xl font-bold text-white tracking-tight">Create Account</h1>
          <p className="text-slate-300 mt-2 font-medium">Register for NexusHR</p>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-500/10 border border-red-500/20 text-red-400 rounded-xl text-sm font-medium flex items-center gap-3 backdrop-blur-md">
            <svg className="w-5 h-5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">Username</label>
            <input
              type="text"
              autoComplete="username"
              className={`w-full px-5 py-3.5 bg-white/5 border ${errors.username ? 'border-red-400/50 focus:ring-red-400' : 'border-white/10 focus:border-teal-400 focus:ring-teal-400'} rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-opacity-20 transition-all backdrop-blur-sm`}
              {...register('username')}
            />
            {errors.username && <p className="mt-1.5 text-xs text-red-400 font-medium">{errors.username.message}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">Email</label>
            <input
              type="email"
              autoComplete="email"
              className={`w-full px-5 py-3.5 bg-white/5 border ${errors.email ? 'border-red-400/50 focus:ring-red-400' : 'border-white/10 focus:border-teal-400 focus:ring-teal-400'} rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-opacity-20 transition-all backdrop-blur-sm`}
              {...register('email')}
            />
            {errors.email && <p className="mt-1.5 text-xs text-red-400 font-medium">{errors.email.message}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">Password</label>
            <input
              type="password"
              autoComplete="new-password"
              className={`w-full px-5 py-3.5 bg-white/5 border ${errors.password ? 'border-red-400/50 focus:ring-red-400' : 'border-white/10 focus:border-teal-400 focus:ring-teal-400'} rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-opacity-20 transition-all backdrop-blur-sm`}
              {...register('password')}
            />
            {errors.password && <p className="mt-1.5 text-xs text-red-400 font-medium">{errors.password.message}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">Role</label>
            <select
              className={`w-full px-5 py-3.5 bg-white/5 border ${errors.role ? 'border-red-400/50 focus:ring-red-400' : 'border-white/10 focus:border-teal-400 focus:ring-teal-400'} rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-opacity-20 transition-all backdrop-blur-sm appearance-none cursor-pointer`}
              style={{ backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%239ca3af' d='M6 8L1 3h10z'/%3E%3C/svg%3E")`, backgroundRepeat: 'no-repeat', backgroundPosition: 'right 1rem center' }}
              {...register('role')}
            >
              <option value="EMPLOYEE" className="bg-slate-800 text-white">Employee</option>
              <option value="MANAGER" className="bg-slate-800 text-white">Manager</option>
              <option value="ADMIN" className="bg-slate-800 text-white">Admin</option>
            </select>
            {errors.role && <p className="mt-1.5 text-xs text-red-400 font-medium">{errors.role.message}</p>}
          </div>

          <Button type="submit" loading={loading} className="w-full mt-8 py-3.5 text-base font-semibold rounded-xl bg-gradient-to-r from-teal-600 to-emerald-500 hover:from-teal-500 hover:to-emerald-400 text-white shadow-lg shadow-teal-500/25 border border-white/10 transition-all">
            {loading ? 'Creating account...' : 'Register'}
          </Button>
        </form>

        <p className="text-center text-sm text-slate-400 mt-8 font-medium">
          Already have an account?{' '}
          <Link to="/login" className="text-teal-400 font-bold hover:text-teal-300 transition-colors">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}